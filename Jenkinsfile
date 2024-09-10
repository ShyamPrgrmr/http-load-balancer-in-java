pipeline {
    agent any

    stages {
        stage('Use API Token') {
            steps {
                withCredentials([string(credentialsId: 'dockerhub-username-and-password', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']) {
                    sh '''
                        echo "Username: $USERNAME"
                        echo "Password: $PASSWORD"
                    '''
                }
            }
        }
    }
}
