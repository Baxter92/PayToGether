# 🔍 Guide de résolution - Recherche Elasticsearch retourne une liste vide

## 📋 Problème identifié

Votre recherche `GET /api/recherche/deals?q=deal` retourne une **liste vide** car :

1. ✅ **Les deals existent en base de données** (PostgreSQL)
2. ❌ **L'index Elasticsearch est vide** (les deals n'ont pas été indexés)

## 🔧 Solution implémentée

### 1. Handler d'indexation automatique créé

**Fichier** : `bff-event/handler/impl/DealSearchIndexHandler.java`

Ce handler écoute les événements de deals et indexe automatiquement :
- ✅ **DealCreatedEvent** → Indexation à la création
- ✅ **DealUpdatedEvent** → Mise à jour de l'index
- ✅ **DealCancelledEvent** → Suppression de l'index

### 2. Événement DealUpdatedEvent créé

**Fichier** : `bff-core/event/DealUpdatedEvent.java`

Événement publié lors de la mise à jour d'un deal pour synchroniser Elasticsearch.

### 3. Publication de DealUpdatedEvent ajoutée

**Fichier** : `bff-core/domaine/impl/DealServiceImpl.java`

La méthode `mettreAJour()` publie maintenant `DealUpdatedEvent` après chaque mise à jour.

## 📝 Étapes à suivre

### Étape 1 : Compiler le projet

```bash
cd /Users/da/Documents/NewProjet/PayToGether
./mvnw clean install -DskipTests
```

### Étape 2 : Redémarrer l'application

```bash
./mvnw -pl modules/bff/bff-configuration spring-boot:run
```

### Étape 3 : Réindexer tous les deals existants

**Avant de tester la recherche**, vous devez réindexer tous les deals existants en base de données vers Elasticsearch.

**Méthode 1 - Via HTTP (RECOMMANDÉ)** :

```http
### Réindexation complète (nécessite authentification admin)
POST http://localhost:8080/api/recherche/deals/reindex
Content-Type: application/json
Authorization: Bearer {{votre_token_admin}}
```

**Méthode 2 - Via curl** :

```bash
curl -X POST http://localhost:8080/api/recherche/deals/reindex \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

**Résultat attendu** :
```
HTTP/1.1 200 OK
Réindexation terminée avec succès
```

### Étape 4 : Vérifier les logs

Surveillez les logs pour confirmer l'indexation :

```
INFO  DealRechercheProviderAdapter - Début de la réindexation de tous les deals
INFO  DealRechercheProviderAdapter - Index nettoyé
INFO  DealRechercheProviderAdapter - Trouvé 1 deals à réindexer
INFO  DealRechercheProviderAdapter - Réindexation terminée : 1 deals indexés
```

### Étape 5 : Tester la recherche

```http
### Recherche par mot-clé
GET http://localhost:8080/api/recherche/deals?q=deal
Content-Type: application/json

### Recherche par ville
GET http://localhost:8080/api/recherche/deals?q=Alberta
Content-Type: application/json

### Recherche par catégorie
GET http://localhost:8080/api/recherche/deals?q=POISSON
Content-Type: application/json
```

**Résultat attendu** :
```json
HTTP/1.1 200 OK
[
  {
    "uuid": "4ea745a4-2187-46c3-b31e-b8bb1b099df5",
    "titre": "Deal mis à jour4",
    "description": "Description modifiée",
    "prixDeal": 1500.00,
    "ville": "Alberta",
    "categorieNom": "POISSON",
    ...
  }
]
```

## 🎯 Vérifications

### 1. Vérifier qu'Elasticsearch est accessible

```bash
curl http://localhost:9200
```

**Résultat attendu** :
```json
{
  "name" : "elasticsearch-xxx",
  "cluster_name" : "paytogether-elasticsearch",
  "version" : {
    "number" : "8.11.0"
  }
}
```

### 2. Vérifier l'index Elasticsearch

```bash
# Voir les index
curl http://localhost:9200/_cat/indices?v

# Compter les documents dans l'index deals
curl http://localhost:9200/deals/_count

# Voir les documents indexés
curl http://localhost:9200/deals/_search?pretty
```

**Résultat attendu après réindexation** :
```json
{
  "count": 1,
  "_shards": {
    "total": 1,
    "successful": 1,
    "failed": 0
  }
}
```

## 🚀 Indexation automatique des nouveaux deals

Désormais, **chaque nouveau deal créé sera automatiquement indexé** dans Elasticsearch grâce au handler `DealSearchIndexHandler`.

**Flux automatique** :
1. ✅ Frontend crée un deal via `POST /api/deals`
2. ✅ `DealService.creer()` publie `DealCreatedEvent`
3. ✅ `DealSearchIndexHandler.handleDealCreated()` indexe dans Elasticsearch
4. ✅ Le deal est immédiatement recherchable via `/api/recherche/deals`

**Pareil pour les mises à jour** :
1. ✅ Frontend met à jour un deal via `PUT /api/deals/{uuid}`
2. ✅ `DealService.mettreAJour()` publie `DealUpdatedEvent`
3. ✅ `DealSearchIndexHandler.handleDealUpdated()` réindexe dans Elasticsearch

## ⚠️ Points importants

### Filtrage par statut PUBLIE

La recherche retourne **uniquement les deals publiés** :

```java
// Dans DealRechercheProviderAdapter.rechercherDeals()
List<DealRechercheModele> resultats = documents.stream()
    .filter(doc -> doc.getStatut() == StatutDeal.PUBLIE)
    .map(searchMapper::versModeleRecherche)
    .collect(Collectors.toList());
```

Votre deal a bien le statut `"statut": "PUBLIE"`, donc il devrait apparaître dans les résultats après réindexation.

### Elasticsearch doit être démarré

Assurez-vous qu'Elasticsearch est bien démarré :

**En local (Docker)** :
```bash
docker-compose up -d elasticsearch
```

**Sur Kubernetes** :
```bash
kubectl get pods -n paytogether -l app=elasticsearch
# Doit être en Running
```

## 📊 Débogage

Si la recherche retourne toujours une liste vide après réindexation :

### 1. Vérifier les logs du backend

```bash
# Logs du service de recherche
grep -i "recherche de deals" logs/application.log

# Logs de l'indexation
grep -i "indexation" logs/application.log
```

### 2. Vérifier la connexion Elasticsearch

**application.yml** :
```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
```

### 3. Vérifier que le deal est bien indexé

```bash
curl http://localhost:9200/deals/_search?q=titre:Deal
```

### 4. Activer les logs DEBUG

**application.yml** :
```yaml
logging:
  level:
    com.ulr.paytogether.provider.adapter.DealRechercheProviderAdapter: DEBUG
    org.springframework.data.elasticsearch: DEBUG
```

## 📝 Résumé

| Action | Commande | Résultat attendu |
|--------|----------|------------------|
| 1. Compiler | `./mvnw clean install` | BUILD SUCCESS |
| 2. Démarrer | `./mvnw spring-boot:run` | Application started |
| 3. Réindexer | `POST /api/recherche/deals/reindex` | 200 OK |
| 4. Rechercher | `GET /api/recherche/deals?q=deal` | Liste de deals |

**Après ces étapes, votre recherche devrait fonctionner ! 🎉**

---

**Date de résolution** : 10 mars 2026  
**Fichiers modifiés** :
- ✅ `bff-event/handler/impl/DealSearchIndexHandler.java` (créé)
- ✅ `bff-core/event/DealUpdatedEvent.java` (créé)
- ✅ `bff-core/domaine/impl/DealServiceImpl.java` (modifié)

