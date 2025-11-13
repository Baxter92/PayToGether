pipeline {
    agent any

    environment {
        REGISTRY = "192.168.49.2:30918"
        IMAGE = "paytogetherapp"
        CHART_DIR = "helm/paytogetherapp"
        KUBECONFIG = credentials('kubeconfig-minikube')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh """
                    docker build -t $REGISTRY/$IMAGE:${env.BRANCH_NAME} .
                    docker push $REGISTRY/$IMAGE:${env.BRANCH_NAME}
                    """
                }
            }
        }

        stage('Deploy on Minikube') {
            when {
                anyOf {
                    branch 'dev'
                    branch 'main'
                }
            }
            steps {
                withEnv(["KUBECONFIG=${KUBECONFIG}"]) {
                    sh """
                    helm upgrade --install paytogetherapp-release $CHART_DIR \
                      --set image.repository=$REGISTRY/$IMAGE \
                      --set image.tag=${env.BRANCH_NAME}
                    """
                }
            }
        }
    }

    post {
        success {
            echo "🎉 Deployment ok!"
        }
    }
}
