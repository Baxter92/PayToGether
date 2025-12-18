# Production Ready Kit — dealtogether.ca

**Contenu**
- Présentation & prérequis
- DNS / domaines
- Installation K3s (HML / PROD)
- Registry privé `registry.dealtogether.ca` (TLS, auth) déployé dans k8s
- Cert-manager (Let’s Encrypt) + Ingress NGINX
- Jenkins (pipeline multi-branch) + ServiceAccount/Kubeconfig
- Manifests Kubernetes (kustomize): frontend (BFF Spring Boot + React), backend, postgres
- Overlays dev / hml / prod
- Prometheus/Grafana (Helm)
- RBAC, NetworkPolicy, sécurité minimale
- Commandes utiles & checklist

---

> **Domains given by user:**
> - BFF (backend+frontend combined app): `devbff.dealtogether.ca`
> - Front (frontend public): `dev.dealtogether.ca`
> - Registry: `registry.dealtogether.ca`

> **Assumptions**
> - You control DNS for `dealtogether.ca` and can create A records pointing to your server(s).
> - You have `Docker`, `kubectl` installed on the server(s).
> - We'll use K3s for HML/Prod (single-node HML and 3-node HA Prod explained).

---

## 0. Quick topology

- k3s cluster (HML / PROD)
- Namespaces: `infra`, `jenkins`, `platform-monitoring`, `dev`, `hml`, `prod`
- Registry deployed as a k8s deployment in `infra` and exposed via Ingress `registry.dealtogether.ca` (TLS)
- Ingress controller: ingress-nginx in `ingress-nginx` namespace
- cert-manager for automatic TLS
- Jenkins deployed (or external) with credentials to push to registry and with Kubeconfig to deploy
- Prometheus & Grafana via Helm (`monitoring` ns)

---

## 1. DNS

Create A records pointing to your public server IP (or internal IP if behind LB):

```
devbff.dealtogether.ca -> <IP_SERVEUR>
dev.dealtogether.ca    -> <IP_SERVEUR>
registry.dealtogether.ca-> <IP_SERVEUR>
```

If you're testing locally, point these hosts in `/etc/hosts` on your machine.

---

## 2. K3s installation (master) — quick

```bash
# On master node
curl -sfL https://get.k3s.io | sudo sh -
# Wait a bit and check
sudo k3s kubectl get nodes
# Copy kubeconfig for your user
sudo cp /etc/rancher/k3s/k3s.yaml $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

**Notes HA**: For production HA you need a load balancer in front of the 3 masters and start k3s with `INSTALL_K3S_VERSION` and additional args; see k3s docs. This kit will give single-node HML and guidance for 3-node PROD.

---

## 3. Install Ingress NGINX & cert-manager

```bash
# ingress-nginx
kubectl create namespace ingress-nginx
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml

# cert-manager
kubectl create namespace cert-manager
kubectl apply --validate=false -f https://github.com/cert-manager/cert-manager/releases/latest/download/cert-manager.yaml

# Create ClusterIssuer for Let's Encrypt (replace email)
cat <<EOF | kubectl apply -f -
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: admin@dealtogether.ca
    privateKeySecretRef:
      name: acme-account-key
    solvers:
    - http01:
        ingress:
          class: nginx
EOF
```

(If you have firewall restrictions, use `letsencrypt-staging` server while testing.)

---

## 4. Local Registry (secure) — `registry.dealtogether.ca`

We'll deploy `registry:2` in `infra` namespace and expose it with an Ingress and TLS via cert-manager.

### 4.1 Namespace and basic manifests

`infra/registry-deployment.yaml`

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: infra
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: registry
  namespace: infra
  labels:
    app: registry
spec:
  replicas: 1
  selector:
    matchLabels:
      app: registry
  template:
    metadata:
      labels:
        app: registry
    spec:
      containers:
        - name: registry
          image: registry:2
          ports:
            - containerPort: 5000
          volumeMounts:
            - mountPath: /var/lib/registry
              name: registry-storage
      volumes:
        - name: registry-storage
          emptyDir: {}
```

`infra/registry-service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: registry
  namespace: infra
spec:
  selector:
    app: registry
  ports:
    - port: 5000
      targetPort: 5000
      protocol: TCP
```

`infra/registry-ingress.yaml`

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: registry-ingress
  namespace: infra
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  rules:
    - host: registry.dealtogether.ca
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: registry
                port:
                  number: 5000
  tls:
    - hosts:
        - registry.dealtogether.ca
      secretName: registry-tls
```

**Apply**

```bash
kubectl apply -f infra/registry-deployment.yaml
kubectl apply -f infra/registry-service.yaml
kubectl apply -f infra/registry-ingress.yaml
```

Wait for cert-manager to issue certificate (check `kubectl get certificate -n infra`).

### 4.2 Insecure vs secure local registry notes

- We recommend TLS via cert-manager/Let's Encrypt. If using self-signed, configure docker daemon `insecure-registries` on nodes and Jenkins.
- If you want auth: add `htpasswd` + `registry` auth via additional config; this doc uses open or network-restricted registry.

---

## 5. Jenkins — install & credentials

You can deploy Jenkins in k8s or use external Jenkins. For simplicity, deploy in `jenkins` namespace with persistence.

`jenkins/jenkins-deployment.yaml` (simplified)

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: jenkins
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jenkins
  namespace: jenkins
spec:
  replicas: 1
  selector:
    matchLabels:
      app: jenkins
  template:
    metadata:
      labels:
        app: jenkins
    spec:
      containers:
        - name: jenkins
          image: jenkins/jenkins:lts-jdk11
          ports:
            - containerPort: 8080
            - containerPort: 50000
```

`jenkins/jenkins-service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: jenkins
  namespace: jenkins
spec:
  selector:
    app: jenkins
  ports:
    - port: 8080
      targetPort: 8080
      nodePort: 32000
    - port: 50000
      targetPort: 50000
      nodePort: 32001
  type: NodePort
```

**Create ServiceAccount & RBAC for Jenkins to deploy**

`jenkins/serviceaccount-rbac.yaml`

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: jenkins-deploy
  namespace: jenkins
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: jenkins-deploy-binding
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
  - kind: ServiceAccount
    name: jenkins-deploy
    namespace: jenkins
```

> Note: `cluster-admin` is permissive. For production lock down permissions to required namespaces.

**Get kubeconfig for Jenkins**

```bash
# create token kubeconfig to use in Jenkins credentials (file)
kubectl -n jenkins create token jenkins-deploy > jenkins-sa-token.txt
# alternative: create kubeconfig with serviceaccount cert/sa secrets (see k8s docs)
```

Add credentials in Jenkins:
- `docker-registry-creds` (username/password) for registry.dealtogether.ca
- `kubeconfig-cred-id` (file) containing kubeconfig with jenkins-deploy SA access

---

## 6. Jenkinsfile (final multi-branch, branch->env mapping)

Place this `Jenkinsfile` at repo root. It automatically chooses target env based on branch name (`dev`, `hml`, `main`). It builds the maven BFF, builds docker image, tags and pushes to registry `registry.dealtogether.ca`, then deploys the kustomize overlay.

````groovy
pipeline {
  agent any

  environment {
    REGISTRY = "registry.dealtogether.ca"
    IMAGE_FRONT = "${env.REGISTRY}/frontend"
    IMAGE_BACK  = "${env.REGISTRY}/backend"
    DOCKER_CRED = 'docker-registry-creds'
    KUBE_CONFIG = 'kubeconfig-cred-id'
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Determine env') {
      steps {
        script {
          if (env.BRANCH_NAME == 'dev') { env.TARGET='dev'; env.TAG='dev' }
          else if (env.BRANCH_NAME == 'hml') { env.TARGET='hml'; env.TAG='hml' }
          else if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master') { env.TARGET='prod'; env.TAG='prod' }
          else { env.TARGET='dev'; env.TAG='dev' }
          echo "Branch=${env.BRANCH_NAME} -> TARGET=${env.TARGET} TAG=${env.TAG}"
        }
      }
    }

    stage('Maven build (BFF)') {
      steps {
        sh "mvn -f frontend/pom.xml clean package -DskipTests"
      }
    }

    stage('Build Docker') {
      steps {
        sh "docker build -t frontend:latest ./frontend"
        sh "docker build -t backend:latest ./backend"
      }
    }

    stage('Push to registry') {
      steps {
        withCredentials([usernamePassword(credentialsId: DOCKER_CRED, usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
          sh '''
            docker login ${REGISTRY} -u $REG_USER -p $REG_PASS
            FRONT_TAG=${REGISTRY}/frontend:${TAG}-${GIT_COMMIT}
            BACK_TAG=${REGISTRY}/backend:${TAG}-${GIT_COMMIT}
            docker tag frontend:latest ${FRONT_TAG}
            docker tag backend:latest ${BACK_TAG}
            docker push ${FRONT_TAG}
            docker push ${BACK_TAG}
          '''
        }
      }
    }

    stage('Deploy K8s') {
      steps {
        withCredentials([file(credentialsId: KUBE_CONFIG, variable: 'KUBECONFIG_FILE')]) {
          sh '''
            mkdir -p $HOME/.kube
            cp $KUBECONFIG_FILE $HOME/.kube/config
            kubectl set image -k k8s/overlays/${TARGET} frontend=${REGISTRY}/frontend:${TAG}-${GIT_COMMIT} --local -o yaml | kubectl apply -f - || true
            kubectl set image -k k8s/overlays/${TARGET} backend=${REGISTRY}/backend:${TAG}-${GIT_COMMIT} --local -o yaml | kubectl apply -f - || true
            kubectl apply -k k8s/overlays/${TARGET}
          '''
        }
      }
    }
  }

  post {
    success { echo "Deployed ${TARGET}" }
    failure { echo 'Build failed' }
  }
}
````

---

## 7. Kubernetes manifests (kustomize) — structure

```
k8s/
├── base/
│   ├── kustomization.yaml
│   ├── frontend-deployment.yaml
│   ├── frontend-service.yaml
│   ├── frontend-jmx-configmap.yaml
│   ├── backend-deployment.yaml
│   ├── backend-service.yaml
│   ├── postgres-statefulset.yaml
│   ├── postgres-service.yaml
│   ├── prometheus-config.yaml
│   ├── prometheus-deployment.yaml
│   ├── prometheus-service.yaml
│   └── ingress.yaml
└── overlays/
    ├── dev/
    │   ├── kustomization.yaml
    │   └── patch-dev.yaml
    ├── hml/
    └── prod/
```

Below are key files — **replace placeholders** `<IP_SERVEUR>` and image tags as built by Jenkins.

### 7.1 `k8s/base/frontend-deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
  labels:
    app: frontend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      containers:
        - name: frontend
          image: registry.dealtogether.ca/frontend:latest
          ports:
            - containerPort: 8080
              name: http
          envFrom:
            - configMapRef:
                name: app-config
            - secretRef:
                name: app-secret
          resources:
            requests:
              cpu: "250m"
              memory: "512Mi"
            limits:
              cpu: "1"
              memory: "1.5Gi"
        - name: jmx-exporter
          image: quay.io/prometheus/jmx-exporter:0.16.1
          args:
            - "8081"
            - "/opt/jmx/jmx-config.yml"
          ports:
            - containerPort: 8081
              name: jmx
          volumeMounts:
            - name: jmx-config
              mountPath: /opt/jmx
      volumes:
        - name: jmx-config
          configMap:
            name: frontend-jmx-config
```

### 7.2 `k8s/base/frontend-service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: frontend
spec:
  type: ClusterIP
  selector:
    app: frontend
  ports:
    - port: 8080
      targetPort: 8080
      name: http
    - port: 8081
      targetPort: 8081
      name: jmx
```

> Note: we expose public access via Ingress; keep service ClusterIP and use Ingress for TLS and host mapping.

### 7.3 `k8s/base/frontend-jmx-configmap.yaml`

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: frontend-jmx-config
data:
  jmx-config.yml: |
    startDelaySeconds: 0
    lowercaseOutputName: true
    lowercaseOutputLabelNames: true
    rules:
      - pattern: ".*"
        name: "jmx_$1"
        type: GAUGE
```

### 7.4 `k8s/base/ingress.yaml` (hosts for BFF + front + registry)

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: app-ingress
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  rules:
    - host: devbff.dealtogether.ca
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: frontend
                port:
                  number: 8080
    - host: dev.dealtogether.ca
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: frontend
                port:
                  number: 8080
    - host: registry.dealtogether.ca
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: registry
                port:
                  number: 5000
  tls:
    - hosts:
        - devbff.dealtogether.ca
        - dev.dealtogether.ca
        - registry.dealtogether.ca
      secretName: dealtogether-tls
```

(Adjust rules: if you separate frontend and bff, point to correct services.)

---

## 8. Prometheus & Grafana

Use Helm `kube-prometheus-stack`:

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
kubectl create ns monitoring
helm install monitoring prometheus-community/kube-prometheus-stack -n monitoring
```

To scrape Spring Boot app:
- Either use `ServiceMonitor` (preferred for operator installs) or use `prometheus.yml` static config in ConfigMap as earlier.

Example `ServiceMonitor` (if prometheus-operator is used):

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: frontend-sm
  namespace: monitoring
  labels:
    release: monitoring
spec:
  selector:
    matchLabels:
      app: frontend
  endpoints:
    - port: http
      path: /actuator/prometheus
      interval: 15s
```

---

## 9. RBAC & NetworkPolicy suggestions

- Create namespaces per environment (`dev`, `hml`, `prod`) and limit Jenkins SA to deploy to those namespaces only.
- Add NetworkPolicies to restrict traffic between namespaces.

Example basic NetworkPolicy to allow only ingress from Ingress Controller to frontend:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-ingress-from-nginx
  namespace: prod
spec:
  podSelector:
    matchLabels:
      app: frontend
  ingress:
    - from:
        - namespaceSelector:
            matchLabels:
              name: ingress-nginx
```

---

## 10. Checklist & commands

1. DNS A records for hosts -> server IP
2. Install K3s on master
3. Install ingress-nginx
4. Install cert-manager
5. Deploy registry in `infra` and Ingress
6. Wait cert issuance
7. Deploy Jenkins in `jenkins` ns
8. Add Jenkins credentials (docker-registry-creds & kubeconfig)
9. Push images via Jenkins pipeline
10. Deploy overlays via `kubectl apply -k k8s/overlays/<env>`

Useful commands:

```bash
kubectl get pods -A
kubectl describe certificate -n infra
kubectl logs -n ingress-nginx <pod> # debug ingress
kubectl get ingress -A
kubectl get svc -n infra
```

---

## 11. Next steps I can perform for you right away

- Generate the full repo tree with all files contents (Jenkinsfile, k8s manifests, overlays) ready to copy/paste or to download as a zip.
- Generate TLS + registry auth example (htpasswd) if you want basic auth.
- Generate `ServiceMonitor` manifests and Grafana dashboards JSON for Spring Boot.


---

**Dis-moi** : veux-tu que je **génère maintenant tous les fichiers** (arborescence complète + contenus) pour que tu les pousses sur GitHub ?

(Je peux créer le ZIP / text bundle dans le canvas pour que tu récupères.)

