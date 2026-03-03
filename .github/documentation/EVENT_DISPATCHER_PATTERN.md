# 📡 Event Dispatcher Pattern - Documentation Complète

## 📋 Vue d'ensemble

Le pattern Event Dispatcher a été implémenté pour permettre une communication asynchrone et découplée entre les différents modules de l'application PayToGether.

**Date** : 4 mars 2026  
**Version** : 1.0.0  
**Modules** : bff-event, bff-event-dispatcher

---

## 🏗️ Architecture

### Structure des modules

```
PayToGether/
├── modules/bff/
│   ├── bff-core/                  # Publie les événements
│   ├── bff-event/                 # Définition des événements et handlers
│   │   ├── model/                 # Événements (suffixe Event)
│   │   ├── handler/               # Handlers (suffixe Handler)
│   │   └── annotation/            # @FunctionalHandler
│   └── bff-event-dispatcher/      # Gestion du dispatch et consommation
│       ├── dispatcher/            # EventDispatcherImpl
│       ├── consumer/              # EventConsumerService
│       ├── entity/                # EventRecordJpa (stockage BD)
│       └── repository/            # EventRecordRepository
```

---

## 🔄 Flux de traitement

```
┌─────────────────┐
│   Core Module   │  1. Publier événement
│   (Publisher)   │─────► dispatch(event)
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ EventDispatcher │  2. Enregistrer en BD
│  (bff-event-    │     status: PENDING
│   dispatcher)   │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│  event_record   │  3. Stockage persistant
│   (Database)    │     + metadata
└─────────────────┘
         │
         ▼
┌─────────────────┐
│EventConsumer    │  4. Récupérer PENDING
│   Service       │     (scheduler 5s)
│ (bff-event-     │
│  dispatcher)    │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Handler         │  5. Exécuter handler
│ @Functional     │     avec retry (max 3)
│ Handler         │
└─────────────────┘
         │
         ├─► SUCCESS ──► status: CONSUMED
         │               + Publier HandlerConsumedEvent ✅
         │
         └─► FAILURE ──► retry ──► status: FAILED
                          + Publier HandlerFailedEvent ✅
                          (si max 3)
```

---

## ⚠️ RÈGLE IMPORTANTE : Publication automatique d'événements de confirmation

### Principe fondamental

**À la fin de tout traitement d'un événement par un handler, le système publie AUTOMATIQUEMENT un événement de confirmation, qu'il soit consommé avec succès ou en échec.**

Cette règle garantit :
- ✅ **Traçabilité complète** : Chaque handler laisse une trace de son exécution
- ✅ **Chaînage d'événements** : Permet de créer des workflows réactifs
- ✅ **Monitoring** : Suivi en temps réel des succès et échecs
- ✅ **Audit** : Historique complet de tous les traitements

### Événements de confirmation

#### 1. HandlerConsumedEvent (Succès)

Publié automatiquement quand un handler se termine **sans exception**.

```java
@Getter
public class HandlerConsumedEvent extends DomainEvent {
    private UUID originalEventId;        // ID de l'événement original
    private String originalEventType;    // Type de l'événement original
    private String handlerName;          // Nom du handler
    private String message;              // Message de confirmation
    private String additionalData;       // Données additionnelles (optionnel)
}
```

**Exemple** :
```json
{
  "eventId": "uuid-123",
  "eventType": "HandlerConsumedEvent",
  "sourceClass": "HandlerExecutionTracker",
  "originalEventId": "uuid-original",
  "originalEventType": "PaymentMadeEvent",
  "handlerName": "PaymentHandler.handlePaymentMade",
  "message": "Handler executed successfully",
  "occurredOn": "2026-03-04T10:30:00"
}
```

#### 2. HandlerFailedEvent (Échec)

Publié automatiquement quand un handler lance **une exception**.

```java
@Getter
public class HandlerFailedEvent extends DomainEvent {
    private UUID originalEventId;        // ID de l'événement original
    private String originalEventType;    // Type de l'événement original
    private String handlerName;          // Nom du handler
    private String errorMessage;         // Message d'erreur
    private String exceptionClass;       // Classe de l'exception
    private Integer attemptNumber;       // Numéro de tentative
    private Boolean isFinalFailure;      // true si max tentatives atteint
}
```

**Exemple** :
```json
{
  "eventId": "uuid-456",
  "eventType": "HandlerFailedEvent",
  "sourceClass": "HandlerExecutionTracker",
  "originalEventId": "uuid-original",
  "originalEventType": "PaymentMadeEvent",
  "handlerName": "PaymentHandler.handlePaymentMade",
  "errorMessage": "Connection timeout",
  "exceptionClass": "java.net.SocketTimeoutException",
  "attemptNumber": 2,
  "isFinalFailure": false,
  "occurredOn": "2026-03-04T10:30:05"
}
```

### Implémentation automatique

Le développeur **n'a RIEN à faire** pour publier ces événements. Le système s'en charge automatiquement dans `EventConsumerService`.

```java
@Component
public class MyHandler implements ConsumerHandler {
    
    @FunctionalHandler(eventType = MyEvent.class)
    public void handleMyEvent(MyEvent event) {
        // Votre logique métier
        doSomething(event);
        
        // ✅ HandlerConsumedEvent sera publié automatiquement
        // PAS BESOIN de le publier manuellement !
    }
}
```

Si une exception est lancée :

```java
@FunctionalHandler(eventType = MyEvent.class)
public void handleMyEvent(MyEvent event) {
    try {
        doSomething(event);
    } catch (Exception e) {
        log.error("Error: {}", e.getMessage());
        throw e; // Relancer l'exception
        
        // ✅ HandlerFailedEvent sera publié automatiquement
        // PAS BESOIN de le publier manuellement !
    }
}
```

### Cas d'usage des événements de confirmation

#### 1. Workflow en chaîne

```java
// Handler 1 : Traiter le paiement
@FunctionalHandler(eventType = PaymentMadeEvent.class)
public void handlePayment(PaymentMadeEvent event) {
    processPayment(event);
    // → HandlerConsumedEvent publié
}

// Handler 2 : Déclencher la préparation SEULEMENT après confirmation
@FunctionalHandler(eventType = HandlerConsumedEvent.class)
public void onPaymentConfirmed(HandlerConsumedEvent event) {
    if ("PaymentMadeEvent".equals(event.getOriginalEventType())) {
        startPreparation(event.getOriginalEventId());
    }
}
```

#### 2. Monitoring et alerting

```java
// Handler pour surveiller les échecs
@FunctionalHandler(eventType = HandlerFailedEvent.class)
public void monitorFailures(HandlerFailedEvent event) {
    if (event.getIsFinalFailure()) {
        // Alerter l'équipe technique
        alertService.sendCriticalAlert(
            "Handler " + event.getHandlerName() + " failed permanently",
            event.getErrorMessage()
        );
    }
}
```

#### 3. Audit et statistiques

```java
// Handler pour audit
@FunctionalHandler(eventType = HandlerConsumedEvent.class)
public void auditSuccess(HandlerConsumedEvent event) {
    auditService.logHandlerExecution(
        event.getHandlerName(),
        event.getOriginalEventType(),
        "SUCCESS"
    );
}

@FunctionalHandler(eventType = HandlerFailedEvent.class)
public void auditFailure(HandlerFailedEvent event) {
    auditService.logHandlerExecution(
        event.getHandlerName(),
        event.getOriginalEventType(),
        "FAILURE",
        event.getErrorMessage()
    );
}
```

### Avantages de cette règle

✅ **Pas de code boilerplate** : Publication automatique, rien à coder  
✅ **Cohérence garantie** : Tous les handlers suivent la même règle  
✅ **Traçabilité totale** : Chaque exécution laisse une trace  
✅ **Chaînage facile** : Créer des workflows réactifs  
✅ **Monitoring simplifié** : Suivre succès/échecs en temps réel  
✅ **Audit complet** : Historique de toutes les exécutions  

### Requêtes SQL pour exploiter les événements de confirmation

```sql
-- Tous les handlers qui ont consommé un événement spécifique
SELECT * FROM event_record 
WHERE event_type = 'HandlerConsumedEvent'
AND payload::json->>'originalEventId' = 'uuid-de-evenement';

-- Taux de succès par type d'événement
SELECT 
    payload::json->>'originalEventType' as event_type,
    COUNT(*) as total_consumed
FROM event_record 
WHERE event_type = 'HandlerConsumedEvent'
GROUP BY event_type;

-- Handlers avec le plus d'échecs
SELECT 
    payload::json->>'handlerName' as handler,
    COUNT(*) as failures
FROM event_record 
WHERE event_type = 'HandlerFailedEvent'
AND payload::json->>'isFinalFailure' = 'true'
GROUP BY handler
ORDER BY failures DESC;

-- Timeline complète d'un événement (original + confirmations)
SELECT 
    event_type,
    occurred_on,
    CASE 
        WHEN event_type = 'HandlerConsumedEvent' THEN payload::json->>'handlerName'
        WHEN event_type = 'HandlerFailedEvent' THEN payload::json->>'errorMessage'
        ELSE 'Original event'
    END as details
FROM event_record
WHERE event_id = 'uuid-original'
   OR payload::json->>'originalEventId' = 'uuid-original'
ORDER BY occurred_on;
```

---

## 📦 Module bff-event

### Classes principales

#### 1. DomainEvent (classe abstraite)
```java
public abstract class DomainEvent {
    private UUID eventId;              // ID unique
    private LocalDateTime occurredOn;   // Date création
    private String sourceClass;         // Classe émettrice
    private String eventType;           // Type événement
    private Integer version;            // Version
}
```

#### 2. @FunctionalHandler (annotation)
```java
@FunctionalHandler(
    eventType = PaymentMadeEvent.class,
    maxAttempts = 3,
    description = "Traite les paiements"
)
public void handlePaymentMade(PaymentMadeEvent event) {
    // Logique métier
}
```

#### 3. ConsumerHandler (interface)
```java
public interface ConsumerHandler {
    default boolean canHandle(DomainEvent event);
    default Class<? extends DomainEvent> getEventType();
}
```

#### 4. EventDispatcher (interface)
```java
public interface EventDispatcher {
    void dispatch(DomainEvent event);
    void dispatchAsync(DomainEvent event);
}
```

---

## 🚀 Module bff-event-dispatcher

### Classes principales

#### 1. EventRecordJpa (entité BD)
```java
@Entity
@Table(name = "event_record")
public class EventRecordJpa {
    private UUID eventId;
    private String eventType;
    private String sourceClass;
    private String payload;             // JSON
    private EventStatus status;         // PENDING, PROCESSING, CONSUMED, FAILED
    private Integer attempts;           // Nombre tentatives
    private Integer maxAttempts;        // Max 3
    private LocalDateTime lastAttemptAt;
    private LocalDateTime consumedAt;
    private LocalDateTime failedAt;
    private String errorMessage;
    private String consumerHandler;     // Handler qui a consommé
}
```

**Statuts** :
- `PENDING` : En attente de traitement
- `PROCESSING` : En cours de traitement
- `CONSUMED` : Consommé avec succès
- `FAILED` : Échec après max tentatives

#### 2. EventDispatcherImpl
```java
@Service
public class EventDispatcherImpl implements EventDispatcher {
    
    @Transactional
    public void dispatch(DomainEvent event) {
        // 1. Sérialiser événement en JSON
        String payload = serializeEvent(event);
        
        // 2. Créer enregistrement BD
        EventRecordJpa eventRecord = EventRecordJpa.builder()
            .eventId(event.getEventId())
            .eventType(event.getEventType())
            .payload(payload)
            .status(PENDING)
            .build();
        
        // 3. Sauvegarder
        eventRecordRepository.save(eventRecord);
    }
}
```

#### 3. EventConsumerService
```java
@Service
public class EventConsumerService {
    
    // Découvre tous les handlers annotés @FunctionalHandler
    private void discoverHandlers();
    
    // Traite les événements PENDING toutes les 5 secondes
    @Scheduled(fixedDelay = 5000)
    public void processePendingEvents();
    
    // Traite un événement avec retry
    @Transactional
    public void processEvent(EventRecordJpa eventRecord);
    
    // Réinitialise les événements bloqués
    private void resetStuckEvents();
}
```

---

## 💡 Utilisation

### 1. Créer un événement

```java
// Dans bff-event/model/
@Getter
@NoArgsConstructor
public class DealCreatedEvent extends DomainEvent {
    
    private UUID dealUuid;
    private String titre;
    private BigDecimal prixDeal;
    private UUID createurUuid;
    
    public DealCreatedEvent(UUID dealUuid, String titre, BigDecimal prixDeal, UUID createurUuid) {
        super("DealService");  // Classe source
        this.dealUuid = dealUuid;
        this.titre = titre;
        this.prixDeal = prixDeal;
        this.createurUuid = createurUuid;
    }
    
    @Override
    public String toJson() {
        // Sérialisation JSON
    }
}
```

### 2. Publier un événement (dans bff-core)

```java
@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {
    
    private final DealProvider dealProvider;
    private final EventDispatcher eventDispatcher;
    
    @Override
    public DealModele creer(DealModele deal) {
        // 1. Logique métier
        DealModele saved = dealProvider.sauvegarder(deal);
        
        // 2. Publier événement
        DealCreatedEvent event = new DealCreatedEvent(
            saved.getUuid(),
            saved.getTitre(),
            saved.getPrixDeal(),
            saved.getCreateurUuid()
        );
        eventDispatcher.dispatch(event);
        
        return saved;
    }
}
```

### 3. Créer un handler (dans bff-event/handler/)

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class DealHandler implements ConsumerHandler {
    
    private final NotificationService notificationService;
    private final EmailService emailService;
    
    @FunctionalHandler(
        eventType = DealCreatedEvent.class,
        maxAttempts = 3,
        description = "Notifie la création d'un deal"
    )
    public void handleDealCreated(DealCreatedEvent event) {
        log.info("Handling DealCreatedEvent: {}", event.getDealUuid());
        
        try {
            // 1. Envoyer notification au créateur
            notificationService.notifierCreation(event.getCreateurUuid(), event.getTitre());
            
            // 2. Envoyer email
            emailService.envoyerConfirmationDeal(event);
            
            // 3. Notifier les followers
            notificationService.notifierFollowers(event.getCreateurUuid(), event);
            
            log.info("DealCreatedEvent handled successfully");
            
        } catch (Exception e) {
            log.error("Error handling DealCreatedEvent: {}", e.getMessage(), e);
            throw e; // Relancer pour retry
        }
    }
    
    @FunctionalHandler(
        eventType = DealCreatedEvent.class,
        maxAttempts = 3,
        description = "Met à jour les statistiques"
    )
    public void updateDealStatistics(DealCreatedEvent event) {
        // Logique de statistiques
        statisticsService.incrementerDeals(event.getCreateurUuid());
    }
}
```

---

## 🔧 Configuration

### 1. Dépendances Maven

**bff-core/pom.xml** :
```xml
<dependency>
    <groupId>com.ulr.paytogether</groupId>
    <artifactId>bff-event</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>com.ulr.paytogether</groupId>
    <artifactId>bff-event-dispatcher</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. Base de données

```sql
CREATE TABLE event_record (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    source_class VARCHAR(255) NOT NULL,
    occurred_on TIMESTAMP NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 3,
    last_attempt_at TIMESTAMP,
    consumed_at TIMESTAMP,
    failed_at TIMESTAMP,
    error_message TEXT,
    consumer_handler VARCHAR(255),
    version BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_event_status ON event_record(status);
CREATE INDEX idx_event_type ON event_record(event_type);
```

---

## ⚙️ Fonctionnement détaillé

### Retry Logic

1. **Tentative 1** : Event status = PROCESSING
   - Si succès → CONSUMED
   - Si échec → PENDING (attempts = 1)

2. **Tentative 2** : Event status = PROCESSING
   - Si succès → CONSUMED
   - Si échec → PENDING (attempts = 2)

3. **Tentative 3** : Event status = PROCESSING
   - Si succès → CONSUMED
   - Si échec → **FAILED** (attempts = 3)

### Gestion des événements bloqués

- Si un événement est en `PROCESSING` depuis plus de 5 minutes
- Il est automatiquement réinitialisé en `PENDING`
- Cela évite les deadlocks

### Scheduler

```java
@Scheduled(fixedDelay = 5000, initialDelay = 10000)
public void processePendingEvents() {
    // Traite les événements PENDING toutes les 5 secondes
    // Délai initial de 10 secondes au démarrage
}
```

---

## 📊 Exemples d'événements

### DealPublishedEvent
```java
public class DealPublishedEvent extends DomainEvent {
    private UUID dealUuid;
    private String titre;
    private LocalDateTime datePublication;
}
```

### UserRegisteredEvent
```java
public class UserRegisteredEvent extends DomainEvent {
    private UUID userUuid;
    private String email;
    private String nom;
}
```

### CommandeValidatedEvent
```java
public class CommandeValidatedEvent extends DomainEvent {
    private UUID commandeUuid;
    private UUID utilisateurUuid;
    private BigDecimal montantTotal;
}
```

---

## ✅ Avantages du pattern

### Découplage
✅ Les modules ne se connaissent pas directement  
✅ Le core publie, le event consomme  
✅ Pas de dépendance cyclique  

### Fiabilité
✅ Tous les événements sont persistés en BD  
✅ Retry automatique (max 3 tentatives)  
✅ Traçabilité complète (qui, quand, statut)  

### Évolutivité
✅ Ajout de nouveaux handlers sans modifier le core  
✅ Plusieurs handlers pour un même événement  
✅ Facile à étendre (ex: Kafka plus tard)  

### Asynchrone
✅ Traitement différé (scheduler 5s)  
✅ Ne bloque pas le flux principal  
✅ Dispatch async disponible  

---

## 🚀 Extension future : Kafka

Le module est conçu pour faciliter l'intégration de Kafka :

```java
// Nouvelle implémentation dans bff-event-dispatcher
@Service
public class KafkaEventDispatcherImpl implements EventDispatcher {
    
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;
    
    @Override
    public void dispatch(DomainEvent event) {
        // 1. Enregistrer en BD (audit)
        eventRecordRepository.save(event);
        
        // 2. Publier sur Kafka
        kafkaTemplate.send("events-topic", event);
    }
}
```

**Pas de changement dans le module bff-event** !

---

## 📝 Conventions de nommage

### Événements
- ✅ Suffixe `Event`
- ✅ Nom au passé (action accomplie)
- ✅ Exemples : `DealCreatedEvent`, `PaymentMadeEvent`, `UserRegisteredEvent`

### Handlers
- ✅ Suffixe `Handler`
- ✅ Nom du domaine métier
- ✅ Exemples : `DealHandler`, `PaymentHandler`, `NotificationHandler`

### Méthodes handler
- ✅ Préfixe `handle` ou verbe métier
- ✅ Exemples : `handleDealCreated()`, `sendNotification()`, `updateStatistics()`

---

## 🔍 Monitoring et debugging

### Requêtes SQL utiles

```sql
-- Événements en attente
SELECT * FROM event_record WHERE status = 'PENDING';

-- Événements échoués
SELECT * FROM event_record WHERE status = 'FAILED';

-- Événements par type
SELECT event_type, status, COUNT(*) 
FROM event_record 
GROUP BY event_type, status;

-- Événements d'un deal spécifique
SELECT * FROM event_record 
WHERE payload::json->>'dealUuid' = 'uuid-du-deal';

-- Événements récents (dernière heure)
SELECT * FROM event_record 
WHERE occurred_on > NOW() - INTERVAL '1 hour'
ORDER BY occurred_on DESC;
```

### Logs

```java
// EventDispatcherImpl
log.info("Event {} dispatched successfully", event.getEventId());

// EventConsumerService
log.info("Processing {} pending events", pendingEvents.size());
log.warn("Event {} failed, will retry. Attempt {}/{}", ...);
log.error("Event {} marked as failed: {}", ...);

// Handler
log.info("Handling DealCreatedEvent: {}", event.getDealUuid());
log.info("DealCreatedEvent handled successfully");
```

---

## 📚 Checklist d'implémentation

### Pour créer un nouvel événement

- [ ] Créer classe `{Action}Event` dans `bff-event/model/`
- [ ] Hériter de `DomainEvent`
- [ ] Ajouter attributs métier
- [ ] Implémenter `toJson()`
- [ ] Ajouter `@JsonCreator` pour désérialisation

### Pour créer un handler

- [ ] Créer classe `{Domaine}Handler` dans `bff-event/handler/`
- [ ] Implémenter `ConsumerHandler`
- [ ] Annoter `@Component`
- [ ] Créer méthode avec `@FunctionalHandler`
- [ ] Spécifier `eventType` et `maxAttempts`
- [ ] Implémenter logique métier
- [ ] Gérer les exceptions (relancer pour retry)

### Pour publier un événement

- [ ] Injecter `EventDispatcher` dans le service
- [ ] Créer l'instance de l'événement
- [ ] Appeler `dispatch(event)` ou `dispatchAsync(event)`
- [ ] Vérifier les logs
- [ ] Vérifier la table `event_record`

---

## 🎉 Résumé

✅ **Pattern Event Dispatcher** implémenté  
✅ **2 modules** : bff-event + bff-event-dispatcher  
✅ **Persistance BD** : Tous les événements enregistrés  
✅ **Retry automatique** : Max 3 tentatives  
✅ **Découverte automatique** : Handlers avec @FunctionalHandler  
✅ **Asynchrone** : Scheduler toutes les 5 secondes  
✅ **Extensible** : Prêt pour Kafka ou autre message broker  
✅ **Traçabilité** : Status, tentatives, errors, timestamps  

**Le système d'événements est maintenant opérationnel ! 📡✨🚀**

---

**Version** : 1.0.0  
**Date** : 4 mars 2026  
**Status** : ✅ Implémenté et documenté

