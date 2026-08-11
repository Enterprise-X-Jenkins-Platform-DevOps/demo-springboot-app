pipeline {
    agent {
        label 'java'
    }

    options {
        timestamps()
        timeout(time: 10, unit: 'MINUTES')
    }

    stages {
        stage('Environment') {
            steps {
                sh '''
                    echo "=== NODE ==="
                    echo "$NODE_NAME"

                    echo "=== USER ==="
                    whoami

                    echo "=== HOST ==="
                    hostname

                    echo "=== JAVA ==="
                    java -version

                    echo "=== MAVEN ==="
                    mvn -version

                    echo "=== GIT ==="
                    git --version
                '''
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B clean compile'
            }
        }

        stage('Tests') {
            steps {
                sh 'mvn -B test'
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