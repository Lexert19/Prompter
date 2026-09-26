pipeline {
    agent any

     environment {
            DEPLOY_DIR = '/home/lexert/_projects/Prompter/build/libs'
            JAR_NAME   = 'prompter.jar'
        }

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
                       set -e
                       JAR=\$(ls build/libs/*.jar | grep -v -- '-plain' | head -n1)
                       sudo install -m 644 "\$JAR" "${DEPLOY_DIR}/${JAR_NAME}"
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