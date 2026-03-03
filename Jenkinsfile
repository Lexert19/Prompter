pipeline {
    agent any

    stages {
        stage('Pull and restart') {
            steps {
                script {
                    def projectDir = '/home/lexert/_projects/Prompter'

                    sh """
                        cd ${projectDir}
                        git pull origin master
                        sudo systemctl restart prompter.service
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Service restarted successfully.'
        }
        failure {
            echo 'Failed to pull or restart service.'
        }
    }
}