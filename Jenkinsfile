pipeline {

    agent {
        label 'java'
    }

    options {
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
    }

    stages {

        stage('Environment') {
            steps {
                sh '''
                    echo "=== NODE ==="
                    echo "$NODE_NAME"

                    echo "=== USER ==="
                    whoami

                    echo "=== JAVA ==="
                    java -version

                    echo "=== MAVEN ==="
                    mvn -version
                '''
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B clean compile'
            }
        }

        stage('Tests & Coverage') {
            steps {
                sh 'mvn -B verify'
            }
        }

        stage('Semgrep SAST') {
            steps {
                sh '''
            semgrep scan \
              --config semgrep/rules/java-security.yml \
              --exclude target \
              --json \
              --output semgrep-results.json \
              --error \
              .
        '''
            }
        }

        stage('SCA - Dependency Check') {
            steps {
                withCredentials([
                        string(
                            credentialsId: 'nvd-api-key',
                            variable: 'NVD_API_KEY'
                        )
                    ]) {
                    sh '''
                mvn -B \
                  org.owasp:dependency-check-maven:12.1.3:check \
                  -DnvdApiKeyEnvironmentVariable=NVD_API_KEY \
                  -DdataDirectory=/home/jenkins/.dependency-check \
                  -Dformats=HTML,JSON \
                  -DfailBuildOnCVSS=9
            '''
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube-local') {
                    sh '''
                        mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                          -Dsonar.projectKey=enterprise-x:demo-springboot-app \
                          -Dsonar.projectName=demo-springboot-app \
                          -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B package -DskipTests'
            }
        }
    }

    post {
        always {

            junit allowEmptyResults: true,
            testResults: '**/target/surefire-reports/*.xml'

            archiveArtifacts artifacts: 'semgrep-results.json',
            allowEmptyArchive: true

            archiveArtifacts artifacts: '**/target/dependency-check-report.html',
            allowEmptyArchive: true

            archiveArtifacts artifacts: '**/target/dependency-check-report.json',
            allowEmptyArchive: true
        }

        success {
            echo 'CI SUCCESS'
        }

        failure {
            echo 'CI FAILED'
        }
    }
}