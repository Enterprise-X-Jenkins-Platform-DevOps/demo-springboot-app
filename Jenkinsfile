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
              --config auto \
              --exclude target \
              --json \
              --output semgrep-results.json \
              . || true
        '''
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
        }

        success {
            echo 'CI SUCCESS'
        }

        failure {
            echo 'CI FAILED'
        }
    }
}