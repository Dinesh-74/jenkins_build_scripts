pipeline {
    agent any

    tools {
        nodejs "nodejs-20" // Replace with the actual NodeJS tool name configured in Jenkins
    }

    environment {
        CI = 'true'
        NVM_DIR = '/root/.nvm'
        NODE_VERSION = '20.11.0'
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
        stage('Checkout') {
            steps {
                git branch: 'demo', url: 'https://github.com/Dossiernexus/DossierNexus-React.git', credentialsId: 'jenkins_to_github'
            }
        }

        stage('Install dependencies') {
            steps {
                sh 'npm i -g yarn'
                sh 'yarn install --no-bin-links'
                sh 'yarn add vite'
            }
        }
        stage('Check Node and Yarn versions') {
            steps {
                sh 'node -v'
                sh 'yarn -v'
            }
        }
        stage('Check Environment Variables') {
            steps {
                sh 'env'
            }
        }
        stage('Build') {
            steps {
                script {
                    sh 'yarn run build'
                }
            }
        }
        stage('Deploy') {
            steps {
                sh 'sudo rm -rf /home/dossier/domains/test.dossier.nexus/public_html/'
                sh 'sudo cp -r ${WORKSPACE}/build /home/dossier/domains/test.dossier.nexus/public_html/'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
