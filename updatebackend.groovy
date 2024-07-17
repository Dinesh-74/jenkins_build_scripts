pipeline {
    agent any

    tools {
        nodejs "nodejs-18" // Replace with the actual NodeJS tool name configured in Jenkins
    }

    environment {
        CI = 'true'
        PM2_PATH = '/usr/bin/pm2' // Typically, PM2 global installation for root will be here
        NPM_PATH='/var/lib/jenkins/tools/jenkins.plugins.nodejs.tools.NodeJSInstallation/nodejs-18/bin/npm'
        DEP_DIR='/home/a1matrimony/node-app'
        APP_NAME='A1 Matrimony (3004)'
    }
/// Multi branch by plane script Management ///
    // triggers {
    //     GenericTrigger(
    //         genericVariables: [
    //             [key: 'ref', value: '$.ref'],
    //             [key: 'action', value: '$.action'],
    //             [key: 'pull_request_branch', value: '$.pull_request.head.ref']
    //         ],
    //         causeString: 'Triggered on $ref',
    //         token: '4bjKEuaz87q5NSltLOBangl/oIwHoKcdVIKxzsiJZKY=', // Use the secret token you configured in GitHub webhook
    //         printContributedVariables: true,
    //         printPostContent: true,
    //         silentResponse: false,
    //         regexpFilterExpression: '^refs/heads/main$|^main$', pipe biryani
    //         regexpFilterText: '$ref $pull_request_branch'
    //     )
    // }
    
    stages {
        stage('Checkout & Pull') {
            steps {
                git branch: 'master', url: 'https://github.com/nutzdev/A1-Matrimony-BackEnd.git', credentialsId: 'jenkins_to_github'
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

        stage('Install PM2 and Dependencies') {
            steps {
                script {
                    sh '''
                    node -v
                    npm install;
                    '''
                }
            }
        }

        stage('Prepare Deployment Directory') {
            steps {
                script {
                    sh '''
                    sudo mkdir -p ${DEP_DIR}
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                 script {
                    def appIsRunning = sh(script: "${PM2_PATH} ls | grep ${APP_NAME}", returnStatus: true) == 0
                    if (appIsRunning) {
                        sh "sudo cp -r ${WORKSPACE}/. ${DEP_DIR}"
                        sh "sudo ${PM2_PATH} restart 'Dossier-Staging(4030)'"
                    } else {
                        sh '''
                        sudo cp -r ${WORKSPACE}"/. ${DEP_DIR}
                        sudo chown -R root:root ${DEP_DIR}
                        ls -l /home/api/
                        cd ${DEP_DIR}
                        ls -l
                        sudo ${NPM_PATH} run deploy
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
