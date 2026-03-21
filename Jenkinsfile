pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean assemble'
            }
        }
        stage('Test') {
            steps {
                sh 'cp /home/lexert/_projects/Prompter/.env ./.env'
                sh './gradlew test --rerun-tasks'
            }
            post { always { junit 'build/test-results/test/**/*.xml' } }
        }
        stage('Package') {
            steps { sh './gradlew bootJar' }
        }
        stage('Deploy to Production') {
            steps {
                script {
                    sh """
                        cd /home/lexert/_projects/Prompter
                        git pull origin master
                        sudo systemctl restart prompter.service
                    """
                }
            }
        }
    }
    post {
        success { echo 'Pipeline succeeded.' }
        failure { echo 'Pipeline failed.' }
    }
}