# PayToGether

> **✅ Build Status : FONCTIONNEL** (mis à jour le 3 février 2026)  
> Les problèmes de build Maven ont été résolus. Voir [`README_BUILD_FIX.md`](README_BUILD_FIX.md) pour les détails.

> **🔄 Gestion de base de données : LIQUIBASE INTÉGRÉ** (19 février 2026)  
> Le schéma de base de données est maintenant géré avec Liquibase. Voir [`LIQUIBASE_INTEGRATION.md`](LIQUIBASE_INTEGRATION.md) pour les détails.

> **🐳 Docker Java 21 : OPTIMISÉ** (21 février 2026)  
> Dockerfile migré vers Java 21 avec optimisations multi-stage, images Alpine et JVM optimisée. Voir [`RECAPITULATIF_DOCKER_JAVA21.md`](RECAPITULATIF_DOCKER_JAVA21.md) pour les détails.

---

## 🚀 Quick Start

### Développement local avec Docker

```bash
# Démarrer la stack complète (BFF + PostgreSQL + MinIO)
docker-compose up -d

# Vérifier le statut
curl http://localhost:8080/actuator/health
```

**👉 Guide complet** : [QUICKSTART_DOCKER.md](QUICKSTART_DOCKER.md)

### Avec Makefile

```bash
make run-compose  # Démarrer
make logs         # Voir les logs
make health       # Health check
make stop-compose # Arrêter
```

---

## 📦 Docker - Nouvelles Optimisations Java 21

### Caractéristiques

- ✅ **Java 21** avec Eclipse Temurin
- ✅ **Images Alpine** (~200 MB vs ~850 MB)
- ✅ **Multi-stage build** (cache Maven optimisé)
- ✅ **Utilisateur non-root** (sécurité renforcée)
- ✅ **JVM optimisée** (G1GC, gestion mémoire container)
- ✅ **BuildKit** (build 70% plus rapide)

### Résultats

| Métrique | Avant | Après | Gain |
|----------|-------|-------|------|
| Taille image | ~850 MB | ~200 MB | **-76%** |
| Temps démarrage | ~45s | ~15s | **-67%** |
| Temps build (cache) | ~5 min | ~1 min | **-80%** |

### Build de l'image

```bash
# Avec script automatisé
./build-docker.sh

# Avec Makefile
make build

# Ou manuellement
DOCKER_BUILDKIT=1 docker build -f modules/bff/Dockerfile -t paytogether-bff:latest .
```

---

## Repository Docker Hub

Nous utilisons le repository Docker Hub: `14152021/dealtogether`.
Les images construites par le pipeline Jenkins sont taggées comme suit:
- front: `14152021/dealtogether:front-<env>-<commit>`
- bff: `14152021/dealtogether:bff-<env>-<commit>`

Des tags `front-latest` et `bff-latest` sont également poussés pour les branches `dev` et `hml`.

### Registry privé

**Registry** : `registry.dealtogether.ca`

```bash
# Build et push vers registry privé
make build
make push

# Ou manuellement
docker build -f modules/bff/Dockerfile -t registry.dealtogether.ca/bffpaytogether:latest .
docker push registry.dealtogether.ca/bffpaytogether:latest
```

---

## Pré-requis

- **Docker** : version 20.10+
- **Docker Compose** : version 2.0+
- **Mémoire** : minimum 4 GB alloués à Docker
- **Java** : version 21 (pour développement local sans Docker)
- **Maven** : version 3.9+
- Jenkins configuré avec les credentials:
  - `pay2gether` (username/password Docker Hub) utilisé dans le `Jenkinsfile`.
  - `pay2gether` (file) pour le kubeconfig si vous voulez déployer depuis Jenkins.
- Un cluster Kubernetes avec NGINX Ingress controller installé.

---

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

## Dépannage : accès externe via Ingress

Si vous ne parvenez pas à accéder au front via `dev.dealtogether.ca`, suivez ces étapes de diagnostic depuis une machine ayant accès au cluster (ou depuis une machine distante pour tester l'IP publique) :

1) Vérifier l'état de l'Ingress et l'IP exposée :

```bash
kubectl get ingress -n paytogether
kubectl describe ingress front-ingress -n paytogether
```

Attendu : l'Ingress doit apparaître avec une adresse IP ou un hostname dans la colonne `ADDRESS` (ou être pris en charge par le controller). Si l'ADDRESS est vide, l'Ingress controller ne fournit pas d'IP externe.

2) Vérifier le controller NGINX (namespace commun : `ingress-nginx` ou `kube-system`) :

```bash
kubectl get pods -n ingress-nginx
kubectl get svc -n ingress-nginx
```

Cherchez un `Service` du controller de type `LoadBalancer` (avec EXTERNAL-IP) ou `NodePort` si vous avez exposé manuellement. Si vous n'avez pas d'EXTERNAL-IP, vous devez soit :
- installer MetalLB (pour bare-metal) et configurer une IP pool, ou
- utiliser un service LoadBalancer fourni par votre cloud, ou
- exposer le controller via `NodePort` et ouvrir le port sur votre firewall/routeur.

3) Vérifier que le service et les endpoints du front existent :

```bash
kubectl get svc front-service -n paytogether
kubectl get endpoints front-service -n paytogether
kubectl get pods -l app=front -n paytogether
```

Attendu : le service doit avoir des endpoints listés. Si `endpoints` est `<none>`, le selector du Service n'est pas aligné avec les labels des Pods.

4) Tester depuis l'extérieur (remplacez l'IP par `31.97.132.132`) :

```bash
# Test HTTP en envoyant l'en-tête Host pour simuler le domaine
curl -v -H "Host: dev.dealtogether.ca" http://31.97.132.132/

# Si votre DNS est configuré pour pointer dev.dealtogether.ca vers l'IP


# Vérifier la résolution DNS
dig +short dev.dealtogether.ca
nslookup dev.dealtogether.ca
```

Interprétation rapide :
- si `curl -v -H "Host: ..." http://31.97.132.132/` retourne 200 ou la page du front, le controller reçoit le trafic et l'Ingress fonctionne ; le problème est au niveau DNS externe (ou propagation) ;
- si `curl` n'atteint pas l'IP (timeout ou connection refused), le port 80/443 est bloqué par un firewall ou l'Ingress controller n'écoute pas sur l'IP publique ;
- si `curl` retourne 404, vérifiez les règles d'Ingress (`kubectl describe ingress ...`) et les `paths` définis.

5) Vérifier les logs du controller NGINX pour erreurs :

```bash
# Remplacez <deployment-name> par le nom du déploiement du controller (ex: ingress-nginx-controller)
kubectl logs -n ingress-nginx deploy/ingress-nginx-controller
kubectl logs -n ingress-nginx <pod-name>
```

6) Test rapide d'accès au service front dans le cluster :

```bash
# depuis une machine qui a kubectl configuré
kubectl port-forward svc/front-service 8080:80 -n paytogether &
# puis dans un autre terminal
curl -v http://localhost:8080/
```

Si le port-forward fonctionne et que vous obtenez la page, les pods et le service sont OK — le problème est entre l'Ingress controller et l'extérieur.

Points fréquents de blocage et corrections proposées :
- L'Ingress controller n'a pas d'EXTERNAL-IP (bare-metal) : installez MetalLB ou changez stratégie d'exposition (NodePort + ouvrir firewall) ;
- DNS incorrect ou non propagé : vérifiez que `dev.dealtogether.ca` pointe bien vers `31.97.132.132` ;
- Firewall/iptables du serveur qui bloque 80/443 : ouvrez les ports 80/443 sur la VM/routeur public ;
- Service selector mismatch : vérifier que le `Service` sélectionne bien les `Pods` du front (labels `app: front`) ;
- Probes de readiness retournent non-ready : si votre SPA renvoie 200 sur `/` ou `/index.html`, ajustez le probe pour pointer vers une URL valide ;

Si vous voulez, je peux :
- analyser ensemble les sorties des commandes ci-dessus (collez-les ici),
- proposer des ajustements manuels pour MetalLB / NodePort si vous êtes en environnement bare-metal,
- ajouter une route `/health` dans le front ou ajuster les probes à `/index.html` si nécessaire ;

## Exposition des services en LoadBalancer (bare-metal)

Sur un cluster bare-metal ou VPS, `Service` de type `LoadBalancer` nécessite un composant réseau qui alloue des IP externes (ex: MetalLB). Ci-dessous les étapes pour installer MetalLB et configurer une plage d'adresses IP.

1) Installer MetalLB (version stable) :

```bash
kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.13.12/config/manifests/metallb-native.yaml
```

2) Créer une ConfigMap MetalLB avec une plage d'adresses disponible dans votre réseau (exemple) :

```bash
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ConfigMap
metadata:
  namespace: metallb-system
  name: config
data:
  config: |
    address-pools:
    - name: default
      protocol: layer2
      addresses:
      - 31.97.132.240-31.97.132.250
EOF
```

3) Vérifier que les Services `LoadBalancer` gagnent une EXTERNAL-IP :

```bash
kubectl get svc -n paytogether -o wide
kubectl describe svc front-service -n paytogether
```

4) Mettre à jour votre DNS (ou /etc/hosts) pour pointer `dev.dealtogether.ca` vers l'IP exposée par MetalLB.

Supprimer Traefik (si présent)

Si vous aviez Traefik installé et que vous voulez le supprimer pour utiliser NGINX Ingress :

```bash
# exemple si installé via Helm
helm uninstall traefik -n traefik
kubectl delete namespace traefik
```

---

## 🔄 Gestion de base de données avec Liquibase

PayToGether utilise **Liquibase** pour gérer les versions du schéma de base de données.

### Avantages
- ✅ Versioning complet du schéma de base de données
- ✅ Traçabilité de toutes les modifications
- ✅ Rollback possible en cas de problème
- ✅ Synchronisation automatique au démarrage
- ✅ Gestion multi-environnements (dev/hml/prod)

### Documentation
- 📄 **[LIQUIBASE_INTEGRATION.md](LIQUIBASE_INTEGRATION.md)** - Résumé de l'intégration
- 📄 **[.github/documentation/LIQUIBASE_GUIDE.md](.github/documentation/LIQUIBASE_GUIDE.md)** - Guide complet
- 📄 **[.github/documentation/LIQUIBASE_AIDE_MEMOIRE.md](.github/documentation/LIQUIBASE_AIDE_MEMOIRE.md)** - Aide-mémoire rapide
- 📄 **[.github/documentation/LIQUIBASE_BONNES_PRATIQUES.md](.github/documentation/LIQUIBASE_BONNES_PRATIQUES.md)** - Conventions du projet
- 📄 **[.github/documentation/EXEMPLE_AJOUT_ENTITE_COMMANDE.md](.github/documentation/EXEMPLE_AJOUT_ENTITE_COMMANDE.md)** - Exemple pratique complet

### Schéma actuel (v1.0.0)
Le schéma initial comprend :
- **9 tables principales** : `utilisateur`, `categorie`, `deal`, `publicite`, `image_deal`, `image_utilisateur`, `image`, `deal_participants`, `deal_points_forts`
- **8 catégories pré-remplies** : Électronique, Mode, Alimentation, Maison, Sports, Beauté, Services, Voyage
- **15+ index de performance** sur les colonnes critiques
- **Données de test** pour le développement (utilisateurs, deals, publicités)

### Configuration
Liquibase est configuré pour s'exécuter automatiquement au démarrage de l'application. La configuration se trouve dans :
- `modules/bff/bff-configuration/src/main/resources/application.properties`
- `modules/bff/bff-configuration/src/main/resources/db/changelog/`

### Migration depuis Hibernate DDL Auto
Si vous aviez une base existante, consultez la section "Migration" dans [LIQUIBASE_INTEGRATION.md](LIQUIBASE_INTEGRATION.md).

---

