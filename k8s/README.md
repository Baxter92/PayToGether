# k8s manifests pour paytogether

Prérequis : `kubectl` configuré pour accéder au cluster et un controller Ingress (ex: nginx-ingress) installé.

Ordre recommandé pour appliquer les manifests (incluant le registry local) :

```bash
kubectl apply -f k8s/namespace-paytogether.yaml
# Registry local (PVC + déploiement + service + ingress)
kubectl apply -f k8s/registry-pvc.yaml -n paytogether
kubectl apply -f k8s/registry-deployment.yaml -n paytogether
kubectl apply -f k8s/registry-service.yaml -n paytogether
kubectl apply -f k8s/ingress-registry.yaml -n paytogether

# Configs & Secrets
kubectl apply -f k8s/configmap-bff.yaml -n paytogether
kubectl apply -f k8s/secret-bff.yaml -n paytogether
kubectl apply -f k8s/configmap-front.yaml -n paytogether

# Services & Deployments
kubectl apply -f k8s/service-bff.yaml -n paytogether
kubectl apply -f k8s/service-front.yaml -n paytogether
kubectl apply -f k8s/deployment-bff.yaml -n paytogether
kubectl apply -f k8s/deployment-front.yaml -n paytogether

# Ingress
kubectl apply -f k8s/ingress-bff.yaml -n paytogether
kubectl apply -f k8s/ingress-front.yaml -n paytogether
```

Domaines configurés pour l'Ingress :
- Front : `dev.dealtogether.ca`
- BFF : `devbff.dealtogether.ca`
- Registry local : `registry.dealtogether.ca`

Commandes utiles pour vérification et debug :

- Vérifier les pods et leur statut :

```bash
kubectl get pods -n paytogether
kubectl describe pod <pod-name> -n paytogether
kubectl logs deployment/front-deploiement -n paytogether
kubectl logs deployment/bff-deploiement -n paytogether
kubectl logs deployment/registry -n paytogether
```

- Vérifier services et endpoints :

```bash
kubectl get svc -n paytogether
kubectl describe svc front-service -n paytogether
kubectl describe svc registry -n paytogether
kubectl get endpoints -n paytogether
```

- Vérifier Ingress :

```bash
kubectl get ingress -n paytogether
kubectl describe ingress front-ingress -n paytogether
kubectl describe ingress bff-ingress -n paytogether
kubectl describe ingress registry-ingress -n paytogether
```

- Pour forcer un redéploiement :

```bash
kubectl rollout restart deployment/front-deploiement -n paytogether
kubectl rollout restart deployment/bff-deploiement -n paytogether
kubectl rollout restart deployment/registry -n paytogether
```

- Accéder au registry local (exemple) :

```bash
# Sur votre machine locale, ajouter l'entrée /etc/hosts si nécessaire :
# <INGRESS_CONTROLLER_IP> registry.dealtogether.ca

# Puis se connecter (si pas d'auth):
docker login registry.dealtogether.ca

# Pusher une image locale:
docker tag myimage:latest registry.dealtogether.ca:5000/myimage:latest
docker push registry.dealtogether.ca:5000/myimage:latest
```

- Nettoyage des images côté registry :
  - Dans le registre privé, configure une politique de rétention/expiration.
  - Manuellement, supprimer les tags obsolètes via l'interface ou API du registry.
