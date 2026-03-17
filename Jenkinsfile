pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Build') {
            steps { sh './gradlew clean assemble' }
        }
        stage('Test') {
            steps { sh './gradlew test' }
            post { always { junit 'build/test-results/test/**/*.xml' } }
        }
        stage('Package') {
            steps { sh './gradlew bootJar' }
        }
        stage('Deploy to Production') {
            when { branch 'master' }
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