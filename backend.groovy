pipeline {
    agent any

    tools {
        nodejs "nodejs-18" // Replace with the actual NodeJS tool name configured in Jenkins
    }

    environment {
        CI = 'true'
        PM2_PATH ='/root/.nvm/versions/node/v20.11.0/bin/pm2' 
    }

    triggers {
        GenericTrigger(
            genericVariables: [
                [key: 'ref', value: '$.ref'],
                [key: 'action', value: '$.action'],
                [key: 'pull_request_branch', value: '$.pull_request.head.ref']
            ],
            causeString: 'Triggered on $ref',
            token: 'vdyTy2cIY6MccoOgGWeUsXhF+48UbihIViujYvxF82s=', // Use the secret token you configured in GitHub webhook
            printContributedVariables: true,
            printPostContent: true,
            silentResponse: false,
            regexpFilterExpression: '^refs/heads/demo$|^demo$',
            regexpFilterText: '$ref $pull_request_branch'
        )
    }
    
    stages {
        stage('Checkout & Pull') {
            steps {
                git branch: 'demo', url: 'https://github.com/Dossiernexus/DossierNexus-BackEnd.git', credentialsId: 'jenkins_to_github'
            }
        }

        stage('Check Node Version') {
            steps {
                sh 'node -v'
            }
        }

        stage('Check Environment Variables') {
            steps {
                sh 'env'
            }
        }

        stage('Install Dependencies') {
            steps {
                script {
                    sh "npm install"
                }
            }
        }

        stage('Prepare Deployment Directory') {
            steps {
                script {
                    sh '''
                    sudo rm -r /home/dossier/domains/demo.dossier.nexus/node-app
                    sudo mkdir -p /home/dossier/domains/demo.dossier.nexus/node-app
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                 script {
                    def appIsRunning = sh(script: "sudo ${PM2_PATH} ls | grep 'Dossier-Demo(4001)'", returnStatus: true) == 0
                    if (appIsRunning) {
                        sh "sudo cp -r /var/lib/jenkins/workspace/Development-Backend/'demo-Backend(test)'/. /home/dossier/domains/demo.dossier.nexus/node-app"
                        sh "sudo ${PM2_PATH} restart 'Dossier-Demo(4001)'"
                    } else {
                        sh '''
                        sudo cp -r /var/lib/jenkins/workspace/Development-Backend/"demo-Backend(test)"/. /home/dossier/domains/demo.dossier.nexus/node-app
                        sudo npm run deploy
                        '''
                    }
                }
            }
        }

        stage('Pm2 Management') {
            steps {
                script {
                    sh '''
                    echo "Updating PM2 process list..."
                    sudo ${PM2_PATH} update && sudo ${PM2_PATH} save
                    '''
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
