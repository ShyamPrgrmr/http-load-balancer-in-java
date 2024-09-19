pipeline {
    agent any

    environment {
        DOCKER_CREDENTIALS = credentials('dockerhub-creadentials')
    }

    stages {
        stage('build') {
            steps {
		 
		    sh '''
   			#!/bin/bash
    			echo "..Running build files.."
                	bash runbuild.sh -v $BUILD_NUMBER -r $DOCKER_CREDENTIALS_USR -p $DOCKER_CREDENTIALS_PSW -e $BRANCH_NAME -u $DOCKER_CREDENTIALS_USR

   		    '''
            }
        }
    
    }
}
