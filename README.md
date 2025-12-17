# PayToGether

Documentation rapide pour build, tag et push des images sur Docker Hub et pour activer le registry local (kubernetes).

## Repository Docker Hub
Nous utilisons le repository Docker Hub: `14152021/dealtogether`.
Les images construites par le pipeline Jenkins sont taggées comme suit:
- front: `14152021/dealtogether:front-<env>-<commit>`
- bff: `14152021/dealtogether:bff-<env>-<commit>`

Des tags `front-latest` et `bff-latest` sont également poussés pour les branches `dev` et `hml`.

## Pré-requis
- Jenkins configuré avec les credentials:
  - `pay2gether` (username/password Docker Hub) utilisé dans le `Jenkinsfile`.
  - `pay2gether` (file) pour le kubeconfig si vous voulez déployer depuis Jenkins.
- Un cluster Kubernetes avec NGINX Ingress controller installé.

## Build & Push (local)
Exemples de commandes locales (bash/macOS):

1. Construire les images:

```bash
# depuis la racine du projet
# Front
docker build -t frontpaytogether:latest ./bff-front
# Back
docker build -t bffpaytogether:latest ./
```

2. Tagger et pousser vers Docker Hub (remplacer USER par votre login si nécessaire):

```bash
docker login --username <docker-user>
# Tag
docker tag frontpaytogether:latest 14152021/dealtogether:front-dev-<commit>
docker tag bffpaytogether:latest 14152021/dealtogether:bff-dev-<commit>
# Push
docker push 14152021/dealtogether:front-dev-<commit>
docker push 14152021/dealtogether:bff-dev-<commit>
# Optionnel: latest
docker tag frontpaytogether:latest 14152021/dealtogether:front-latest
docker tag bffpaytogether:latest 14152021/dealtogether:bff-latest
docker push 14152021/dealtogether:front-latest
docker push 14152021/dealtogether:bff-latest
```

## Déployer le registry local (optionnel)
Le manifeste `k8s/registry-deployment.yaml`, `k8s/registry-service.yaml` et `k8s/registry-pvc.yaml` permettent de démarrer un registry Docker local dans le namespace `paytogether`.

1. Appliquer les manifests:

```bash
kubectl apply -f k8s/namespace-paytogether.yaml
kubectl apply -f k8s/registry-pvc.yaml
kubectl apply -f k8s/registry-deployment.yaml
kubectl apply -f k8s/registry-service.yaml
kubectl apply -f k8s/ingress-registry.yaml
```

2. Accéder au registry via `http://registry.dealtogether.ca` (si DNS local ou /etc/hosts configuré vers l'IP de l'ingress).

Note: ce registry n'est pas sécurisé par défaut (HTTP) — pour un usage production, configurez TLS et auth (htpasswd) ou utilisez un registry privé tiers.

## Déploiement des applications
Appliquer tous les manifests k8s:

```bash
kubectl apply -f k8s/ --namespace=paytogether
```

Ceci va créer les déploiements, services et ingresses (assurez-vous que vos enregistrements DNS pointent vers l'IP publique de l'ingress controller).

## Remarques
- Les noms de domaines configurés dans les ingresses sont:
  - Front: `dev.dealtogether.ca`
  - BFF: `devbff.dealtogether.ca`
  - Registry: `registry.dealtogether.ca`

- Le `Jenkinsfile` est configuré pour construire, tagger et pousser vers `14152021/dealtogether` et mettra à jour les déploiements via `kubectl set image`.

Si vous souhaitez que je génère des manifests utilisant directement les variables d'image (e.g., Deployment avec imagePullPolicy: IfNotPresent et un placeholder pour image) ou ajouter TLS pour les ingress, dites-moi et je m'en occupe.
