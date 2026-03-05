# Module BFF-EVENT-DISPATCHER

## Description

Module responsable de la gestion du dispatch et de la consommation des événements.
Ce module implémente la persistance des événements en base de données, le retry automatique, et la découverte des handlers.

## Responsabilités

- Implémenter `EventDispatcher` pour publier les événements
- Persister tous les événements en base de données
- Découvrir automatiquement les handlers annotés `@FunctionalHandler`
- Traiter les événements de manière asynchrone avec retry
- Gérer les statuts des événements (PENDING, PROCESSING, CONSUMED, FAILED)
- Récupérer automatiquement les événements bloqués

## Structure

```
bff-event-dispatcher/
├── dispatcher/         # Implémentation du dispatcher
│   ├── EventDispatcherImpl.java
│   └── EventDispatcherConfiguration.java
├── consumer/           # Consommation des événements
│   └── EventConsumerService.java
├── entity/             # Entités JPA
│   └── EventRecordJpa.java
└── repository/         # Repositories
    └── EventRecordRepository.java
```

## Fonctionnalités

### 1. Dispatch d'événements

```java
@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {
    
    private final EventDispatcher eventDispatcher;
    
    public DealModele creer(DealModele deal) {
        DealModele saved = dealProvider.sauvegarder(deal);
        
        // Publier événement
        eventDispatcher.dispatch(new DealCreatedEvent(saved));
        
        return saved;
    }
}
```

### 2. Consommation automatique

- **Scheduler** : Toutes les 5 secondes
- **Découverte** : Automatique des handlers via `@FunctionalHandler`
- **Retry** : Max 3 tentatives par défaut
- **Recovery** : Événements bloqués réinitialisés après 5 minutes

### 3. Persistance

Table `event_record` :
```sql
- event_id (UUID, PK)
- event_type (VARCHAR)
- source_class (VARCHAR)
- payload (TEXT JSON)
- status (VARCHAR: PENDING, PROCESSING, CONSUMED, FAILED)
- attempts (INTEGER)
- max_attempts (INTEGER)
- consumed_at, failed_at, error_message
```

## Configuration

### Dépendances

```xml
<dependency>
    <groupId>com.ulr.paytogether</groupId>
    <artifactId>bff-event</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

### Properties

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/paytogether
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: none
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml
```

## Flux de traitement

```
1. Service publie événement
   ↓
2. EventDispatcherImpl enregistre en BD (status: PENDING)
   ↓
3. EventConsumerService récupère (scheduler 5s)
   ↓
4. Change status en PROCESSING
   ↓
5. Trouve handlers compatibles
   ↓
6. Exécute handlers
   ↓
7a. Succès → status: CONSUMED
7b. Échec → retry (max 3) → status: FAILED
```

## Monitoring

### Logs

```
INFO  - Event {uuid} dispatched successfully
INFO  - Processing 5 pending events
INFO  - Handler DealHandler.handleDealCreated executed successfully
WARN  - Event {uuid} failed, will retry. Attempt 2/3
ERROR - Event {uuid} marked as failed: {error}
```

### Requêtes SQL

```sql
-- Événements en attente
SELECT * FROM event_record WHERE status = 'PENDING';

-- Événements échoués
SELECT * FROM event_record WHERE status = 'FAILED';

-- Statistiques
SELECT status, COUNT(*) FROM event_record GROUP BY status;
```

## Extensibilité

Le module est conçu pour faciliter l'ajout de nouveaux systèmes de messaging :

```java
// Future implémentation Kafka
@Service
public class KafkaEventDispatcherImpl implements EventDispatcher {
    
    @Override
    public void dispatch(DomainEvent event) {
        // Enregistrer en BD (audit)
        eventRecordRepository.save(event);
        
        // Publier sur Kafka
        kafkaTemplate.send("events", event);
    }
}
```

**Aucun changement nécessaire dans bff-event !**

## Version

1.0.0

