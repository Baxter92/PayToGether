# 🚀 Optimisations de Performance - Table Deal

**Date** : 16 avril 2026  
**Module** : BFF (Backend for Frontend)  
**Impact** : Amélioration significative des performances sur les requêtes en lecture

---

## 📋 Résumé des optimisations

### 1. ✅ Spring Cache (Caffeine)

**Dépendances ajoutées** :
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
  <groupId>com.github.ben-manes.caffeine</groupId>
  <artifactId>caffeine</artifactId>
</dependency>
```

**Configuration** : `CacheConfig.java`
- **TTL** : 5 minutes
- **Taille max** : 1000 entrées
- **Stratégie** : LRU (Least Recently Used)
- **Monitoring** : Stats activées via `recordStats()`

**Caches configurés** :
- `deals` : Cache pour `lireTous()`
- `deal-uuid` : Cache pour `lireParUuid()`
- `deal-statut` : Cache pour `lireParStatut()`
- `deal-createur` : Cache pour `lireParCreateur()`
- `deal-categorie` : Cache pour `lireParCategorie()`
- `villes` : Cache pour `lireVillesDisponibles()`

**Invalidation automatique** :
- `@CacheEvict` sur toutes les méthodes d'écriture (`creer`, `mettreAJour`, `supprimerParUuid`, etc.)

---

### 2. ✅ @Transactional(readOnly = true)

**Impact** :
- **Optimisation Hibernate** : Mode read-only désactive les mécanismes de dirty checking
- **Réduction mémoire** : Pas de snapshot des entités pour comparaison
- **Performances DB** : Certains drivers optimisent les requêtes en lecture seule

**Méthodes optimisées dans `DealServiceImpl`** :
```java
@Transactional(readOnly = true)
- lireParUuid()
- lireTous()
- lireParStatut()
- lireParCreateur()
- lireParCategorie()
- lireVillesDisponibles()
- obtenirUrlLectureImage()
```

**Méthodes optimisées dans `DealProviderAdapter`** :
```java
@Transactional(readOnly = true)
- trouverParUuid()
- trouverTous()
- trouverParStatut()
- trouverParCreateur()
- trouverParCategorie()
- obtenirUrlLectureImage()
- trouverImageParUuid()
- calculerMoyenneCommentaires()
- compterParticipantsReels()
- calculerNombrePartsAchetees()
```

---

### 3. ✅ Index sur la table `deal`

**Migration Flyway** : `V8__ajout_index_performance_deal.sql`

#### Index créés :

| Index | Colonnes | Requête optimisée | Impact |
|-------|----------|-------------------|--------|
| `idx_deal_statut` | `statut` | `findByStatut()` | ⚡️⚡️⚡️ |
| `idx_deal_marchand_uuid` | `marchand_uuid` | `findByMarchandJpa()` | ⚡️⚡️⚡️ |
| `idx_deal_categorie_uuid` | `categorie_uuid` | `findByCategorieJpa()` | ⚡️⚡️⚡️ |
| `idx_deal_statut_favoris_date_creation` | `statut, favoris DESC, date_creation DESC` | `findByStatutOrderByFavorisDescDateCreationDesc()` | ⚡️⚡️⚡️⚡️ |
| `idx_deal_favoris_date_creation` | `favoris DESC, date_creation DESC` | `findAllByOrderByFavorisDescDateCreationDesc()` | ⚡️⚡️⚡️⚡️ |
| `idx_deal_date_expiration` | `date_expiration` | Vérification deals expirés | ⚡️⚡️ |
| `idx_deal_statut_date_expiration` | `statut, date_expiration` | `verifierEtMettreAJourDealsExpires()` | ⚡️⚡️⚡️ |
| `idx_deal_ville` | `ville` | Recherches géographiques | ⚡️⚡️ |
| `idx_deal_date_creation` | `date_creation DESC` | Tris par date | ⚡️⚡️ |

**Note** : Les index composites sont particulièrement efficaces car ils permettent d'éviter les tris en mémoire (covering index).

---

## 📊 Gains de performance attendus

### Avant optimisation
- **lireTous()** : ~200-500ms (requête DB + mapping + statistiques)
- **lireParStatut()** : ~150-300ms (full table scan sans index)
- **lireParCreateur()** : ~100-250ms (recherche sans index)
- **Dirty checking** : Actif sur toutes les transactions

### Après optimisation
- **Première requête** : ~100-200ms (requête DB optimisée par index)
- **Requêtes suivantes** : ~1-5ms ⚡️ (cache Caffeine)
- **Dirty checking** : Désactivé sur les lectures (gain mémoire + CPU)
- **Tri en mémoire** : Évité grâce aux index composites

### Gain global estimé
- **Réduction de 95%** du temps de réponse pour les lectures répétées (cache)
- **Réduction de 50-70%** du temps de requête DB (index)
- **Réduction de 30-40%** de l'utilisation mémoire (readOnly = true)

---

## 🔧 Configuration recommandée

### application.properties (optionnel - monitoring)
```properties
# Activer les logs de cache (développement uniquement)
logging.level.org.springframework.cache=DEBUG

# Activer les statistiques JPA
spring.jpa.properties.hibernate.generate_statistics=true
logging.level.org.hibernate.stat=DEBUG
```

### Monitoring du cache
Accéder aux statistiques via Actuator :
```bash
curl http://localhost:8080/actuator/caches
```

---

## ⚠️ Points d'attention

### 1. Invalidation du cache
Le cache est automatiquement invalidé lors de :
- Création d'un deal (`creer()`)
- Mise à jour d'un deal (`mettreAJour()`, `mettreAJourStatut()`, `basculerFavoris()`)
- Suppression d'un deal (`supprimerParUuid()`)

### 2. TTL du cache
**5 minutes** : Compromis entre fraîcheur des données et performances.
- Augmenter le TTL si les deals changent rarement
- Réduire le TTL si vous avez besoin de données très fraîches

### 3. Taille du cache
**1000 entrées max** : Adapter selon la mémoire disponible.
```java
.maximumSize(1000)  // Modifier dans CacheConfig.java
```

### 4. Index en base de données
- Les index augmentent légèrement le temps d'écriture (~5-10%)
- Gain massif en lecture compense largement ce coût
- PostgreSQL gère automatiquement l'utilisation des index

---

## 🧪 Tests recommandés

### Test de charge avant/après
```bash
# Test avec Apache Bench
ab -n 1000 -c 10 http://localhost:8080/api/deals

# Vérifier les métriques :
# - Time per request
# - Requests per second
# - Connection Times
```

### Vérification des index utilisés
```sql
-- Vérifier qu'un index est utilisé
EXPLAIN ANALYZE SELECT * FROM deal WHERE statut = 'PUBLIE' ORDER BY favoris DESC, date_creation DESC;

-- Devrait afficher "Index Scan using idx_deal_statut_favoris_date_creation"
```

### Vérification du cache
```java
@Test
void testCacheHit() {
    // Première requête (cache miss)
    long start1 = System.currentTimeMillis();
    dealService.lireTous();
    long time1 = System.currentTimeMillis() - start1;
    
    // Deuxième requête (cache hit)
    long start2 = System.currentTimeMillis();
    dealService.lireTous();
    long time2 = System.currentTimeMillis() - start2;
    
    // Le cache hit devrait être ~100x plus rapide
    assertTrue(time2 < time1 / 50);
}
```

---

## 📚 Ressources

- [Spring Cache Documentation](https://docs.spring.io/spring-framework/reference/integration/cache.html)
- [Caffeine Cache](https://github.com/ben-manes/caffeine)
- [PostgreSQL Index Performance](https://www.postgresql.org/docs/current/indexes.html)
- [Hibernate Performance Tuning](https://docs.jboss.org/hibernate/orm/6.0/userguide/html_single/Hibernate_User_Guide.html#performance)

---

## ✅ Checklist de déploiement

- [x] Dépendances Maven ajoutées
- [x] Configuration du cache créée
- [x] `@EnableCaching` activé
- [x] `@Cacheable` sur les méthodes de lecture
- [x] `@CacheEvict` sur les méthodes d'écriture
- [x] `@Transactional(readOnly = true)` sur toutes les lectures
- [x] Migration Flyway V8 créée
- [ ] Tests de charge exécutés
- [ ] Monitoring du cache configuré
- [ ] Documentation mise à jour

---

**Prochaine étape** : Appliquer ces mêmes optimisations sur les autres entités (Utilisateur, Commande, Publicité, etc.)

