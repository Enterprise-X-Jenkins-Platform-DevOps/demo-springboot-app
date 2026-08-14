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
                withCredentials([
                        string(
                            credentialsId: 'semgrep-app-token',
                            variable: 'SEMGREP_APP_TOKEN'
                        )
                    ]) {
                    sh '''
                export SEMGREP_REPO_NAME="Enterprise-X-Jenkins-Platform-DevOps/demo-springboot-app"
                export SEMGREP_REPO_URL="https://github.com/Enterprise-X-Jenkins-Platform-DevOps/demo-springboot-app"
                export SEMGREP_BRANCH="${BRANCH_NAME}"
                export SEMGREP_COMMIT="${GIT_COMMIT}"
                export SEMGREP_JOB_URL="${BUILD_URL}"

                semgrep ci \
                  --exclude target \
                  --exclude .idea \
                  --exclude .mvn
            '''
                }
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
                  org.owasp:dependency-check-maven:12.1.6:check \
                  -DnvdApiKeyEnvironmentVariable=NVD_API_KEY \
                  -DdataDirectory=/home/jenkins/.dependency-check \
                  -Dformats=HTML,JSON \
                  -DfailBuildOnCVSS=9 \
                  -DossIndexAnalyzerEnabled=false
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