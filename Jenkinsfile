pipeline {
  agent any

  environment {
    // Utiliser Docker Hub repository 14152021/dealtogether
    REGISTRY = "docker.io"
    REPOSITORY = "14152021/dealtogether"
    IMAGE_FRONT = "${REPOSITORY}"
    IMAGE_BACK  = "${REPOSITORY}"
    // Noms d'images locales temporaires pour éviter d'écraser les builds
    LOCAL_IMAGE_FRONT = "frontpaytogether"
    LOCAL_IMAGE_BACK = "bffpaytogether"
    KUBE_CRED_ID = "pay2gether"    // id du fichier kubeconfig dans Jenkins
    DOCKER_CRED_ID = "pay2gether"  // id des credentials username/password dans Jenkins
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
        script {
          GIT_COMMIT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
          env.GIT_COMMIT = GIT_COMMIT
          echo "Commit: ${GIT_COMMIT}"
        }
      }
    }

    stage('Determine environment') {
      steps {
        script {
          if (env.BRANCH_NAME == 'dev') {
            env.TARGET_ENV = 'dev'
            env.TAG = "dev"
          } else if (env.BRANCH_NAME == 'hml') {
            env.TARGET_ENV = 'hml'
            env.TAG = "hml"
          } else if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master') {
            env.TARGET_ENV = 'prod'
            env.TAG = "prod"
          } else {
            env.TARGET_ENV = 'main'
            env.TAG = "main"
          }
          echo "Branch=${env.BRANCH_NAME} -> TARGET_ENV=${env.TARGET_ENV} TAG=${env.TAG}"
        }
      }
    }

    stage('Build Back (Maven)') {
      steps {
        sh '''
          set -e
          echo "Building backend (Maven)"
          mvn -f ./pom.xml clean package -DskipTests
        '''
      }
    }

    stage('Build Front (npm)') {
      steps {
        dir('bff-front') {
          sh '''
            set -e
            echo "Building front (npm/vite)"
            npm ci
            npm run build
          '''
        }
      }
    }

    stage('Build Docker images') {
      steps {
        sh '''
          set -e
          echo "Building docker images (local names)"
          docker build -t ${LOCAL_IMAGE_FRONT}:latest ./bff-front
          docker build -t ${LOCAL_IMAGE_BACK}:latest ./
        '''
      }
    }

    stage('Push images to registry') {
      steps {
        withCredentials([usernamePassword(credentialsId: "${DOCKER_CRED_ID}", usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh '''
            set -e
            echo "Login to Docker Hub"
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

            FRONT_TAG=${REPOSITORY}:front-${TAG}-${GIT_COMMIT}
            BACK_TAG=${REPOSITORY}:bff-${TAG}-${GIT_COMMIT}
            LATEST_FRONT_TAG=${REPOSITORY}:front-latest
            LATEST_BACK_TAG=${REPOSITORY}:bff-latest

            echo "Tagging images"
            docker tag ${LOCAL_IMAGE_FRONT}:latest ${FRONT_TAG}
            docker tag ${LOCAL_IMAGE_BACK}:latest ${BACK_TAG}

            echo "Pushing images to ${REPOSITORY}"
            docker push ${FRONT_TAG}
            docker push ${BACK_TAG}

            # Optionnel: pousser également des tags latest pour environnements non prod
            if [ "${TAG}" = "dev" ] || [ "${TAG}" = "hml" ]; then
              docker tag ${LOCAL_IMAGE_FRONT}:latest ${LATEST_FRONT_TAG} || true
              docker tag ${LOCAL_IMAGE_BACK}:latest ${LATEST_BACK_TAG} || true
              docker push ${LATEST_FRONT_TAG} || true
              docker push ${LATEST_BACK_TAG} || true
            fi

            echo "Pushed ${FRONT_TAG} and ${BACK_TAG}"
          '''
          script {
            env.FRONT_TAG = "${REPOSITORY}:front-${env.TAG}-${env.GIT_COMMIT}"
            env.BACK_TAG  = "${REPOSITORY}:bff-${env.TAG}-${env.GIT_COMMIT}"
          }
        }
      }
    }

    stage('Cleanup images') {
      steps {
        sh '''
          set -e
          echo "Cleaning up local docker images to free space"
          docker rmi -f ${LOCAL_IMAGE_FRONT}:latest || true
          docker rmi -f ${LOCAL_IMAGE_BACK}:latest || true
          docker rmi -f ${FRONT_TAG} || true
          docker rmi -f ${BACK_TAG} || true
          docker image prune -af || true
        '''
      }
    }

    stage('Deploy to Kubernetes') {
      steps {
        withCredentials([file(credentialsId: "${KUBE_CRED_ID}", variable: 'KUBECONFIG_FILE')]) {
          sh '''
            set -e
            mkdir -p $HOME/.kube
            cp $KUBECONFIG_FILE $HOME/.kube/config
            chmod 600 $HOME/.kube/config
          '''
          script {
            sh '''
              set -e
              echo "Applying k8s manifests in namespace paytogether"
              kubectl apply -f k8s/ --namespace=paytogether || true

              # Forcibly update images on the named deployments (container names are 'front' and 'bff')
              kubectl set image deployment/front-deploiement front=${FRONT_TAG} --namespace=paytogether || true
              kubectl set image deployment/bff-deploiement bff=${BACK_TAG} --namespace=paytogether || true

              echo "Deploy commands executed"
            '''
          }
        }
      }

    }
   post {
       success {
         echo "Pipeline terminé OK pour ${env.TARGET_ENV}"
       }
       failure {
         echo "Pipeline échoué"
       }
    }
 }
}
