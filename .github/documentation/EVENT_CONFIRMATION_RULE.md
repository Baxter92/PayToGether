# ⚠️ RÈGLE OBLIGATOIRE : Publication automatique d'événements de confirmation

## 📋 Principe fondamental

**À la fin de tout traitement d'un événement par un handler, le système publie AUTOMATIQUEMENT un événement de confirmation, qu'il soit consommé avec succès ou en échec.**

---

## 🎯 Pourquoi cette règle ?

### Avant (sans cette règle)
```java
@FunctionalHandler(eventType = PaymentMadeEvent.class)
public void handlePayment(PaymentMadeEvent event) {
    processPayment(event);
    // ❌ Aucune trace de l'exécution
    // ❌ Impossible de créer des workflows
    // ❌ Monitoring manuel difficile
}
```

### Après (avec cette règle)
```java
@FunctionalHandler(eventType = PaymentMadeEvent.class)
public void handlePayment(PaymentMadeEvent event) {
    processPayment(event);
    // ✅ HandlerConsumedEvent publié automatiquement
    // ✅ Traçabilité garantie
    // ✅ Workflows possibles
    // ✅ Monitoring automatique
}
```

---

## 📡 Les 2 événements de confirmation

### 1️⃣ HandlerConsumedEvent (Succès)

**Quand ?** Le handler se termine sans exception

**Contenu** :
```java
{
  "eventId": "uuid-confirmation",
  "eventType": "HandlerConsumedEvent",
  "sourceClass": "HandlerExecutionTracker",
  "originalEventId": "uuid-event-original",
  "originalEventType": "PaymentMadeEvent",
  "handlerName": "PaymentHandler.handlePaymentMade",
  "message": "Handler executed successfully",
  "occurredOn": "2026-03-04T10:30:00"
}
```

### 2️⃣ HandlerFailedEvent (Échec)

**Quand ?** Le handler lance une exception

**Contenu** :
```java
{
  "eventId": "uuid-echec",
  "eventType": "HandlerFailedEvent",
  "sourceClass": "HandlerExecutionTracker",
  "originalEventId": "uuid-event-original",
  "originalEventType": "PaymentMadeEvent",
  "handlerName": "PaymentHandler.handlePaymentMade",
  "errorMessage": "Connection timeout",
  "exceptionClass": "java.net.SocketTimeoutException",
  "attemptNumber": 2,
  "isFinalFailure": false
}
```

---

## 💻 Comment ça fonctionne ?

### Implémentation dans EventConsumerService

```java
@Service
public class EventConsumerService {
    
    private final EventDispatcher eventDispatcher;
    
    // Après exécution réussie d'un handler
    private void executeHandler(HandlerInfo handlerInfo, DomainEvent event, ...) {
        method.invoke(handlerInstance, event); // Exécuter handler
        
        // ✅ AUTOMATIQUE : Publier événement de confirmation
        publishHandlerConsumedEvent(event, handlerInfo.getHandlerName());
    }
    
    // En cas d'échec
    private void handleFailure(EventRecordJpa eventRecord, String errorMessage) {
        // ✅ AUTOMATIQUE : Publier événement d'échec
        publishHandlerFailedEvent(eventRecord, errorMessage, isFinalFailure);
        
        if (isFinalFailure) {
            markAsFailed(...);
        } else {
            retry();
        }
    }
}
```

### Le développeur n'a RIEN à faire !

```java
@Component
public class MyHandler implements ConsumerHandler {
    
    @FunctionalHandler(eventType = MyEvent.class)
    public void handleMyEvent(MyEvent event) {
        // ✅ Juste votre logique métier
        doSomething(event);
        
        // ✅ HandlerConsumedEvent publié AUTOMATIQUEMENT
        // 🚫 PAS BESOIN de :
        //    eventDispatcher.dispatch(new HandlerConsumedEvent(...));
    }
}
```

---

## 🔗 Cas d'usage

### 1. Workflow en chaîne

Créer des processus métier réactifs basés sur les événements de confirmation.

```java
// Étape 1 : Traiter le paiement
@FunctionalHandler(eventType = PaymentMadeEvent.class)
public void handlePayment(PaymentMadeEvent event) {
    paymentService.process(event);
    // → HandlerConsumedEvent publié
}

// Étape 2 : Déclencher la livraison SEULEMENT après confirmation du paiement
@FunctionalHandler(eventType = HandlerConsumedEvent.class)
public void onPaymentConfirmed(HandlerConsumedEvent event) {
    if ("PaymentMadeEvent".equals(event.getOriginalEventType()) &&
        "PaymentHandler.handlePayment".equals(event.getHandlerName())) {
        
        // Déclencher la livraison
        deliveryService.start(event.getOriginalEventId());
    }
}

// Étape 3 : Gérer les échecs de paiement
@FunctionalHandler(eventType = HandlerFailedEvent.class)
public void onPaymentFailed(HandlerFailedEvent event) {
    if ("PaymentMadeEvent".equals(event.getOriginalEventType()) &&
        event.getIsFinalFailure()) {
        
        // Annuler la commande
        orderService.cancel(event.getOriginalEventId(), event.getErrorMessage());
        
        // Rembourser l'utilisateur
        refundService.process(event.getOriginalEventId());
    }
}
```

**Résultat** : Processus métier complet géré automatiquement !

### 2. Monitoring en temps réel

Surveiller la santé du système et alerter en cas de problème.

```java
@Component
public class SystemMonitoringHandler implements ConsumerHandler {
    
    private final AlertService alertService;
    private final MetricsService metricsService;
    
    // Tracker les succès
    @FunctionalHandler(eventType = HandlerConsumedEvent.class)
    public void trackSuccess(HandlerConsumedEvent event) {
        metricsService.incrementSuccess(
            event.getOriginalEventType(),
            event.getHandlerName()
        );
    }
    
    // Alerter sur les échecs
    @FunctionalHandler(eventType = HandlerFailedEvent.class)
    public void alertOnFailure(HandlerFailedEvent event) {
        metricsService.incrementFailure(
            event.getOriginalEventType(),
            event.getHandlerName()
        );
        
        // Alerte critique si échec final
        if (event.getIsFinalFailure()) {
            alertService.sendCriticalAlert(
                "Handler Failure",
                String.format(
                    "Handler %s failed permanently for %s: %s",
                    event.getHandlerName(),
                    event.getOriginalEventType(),
                    event.getErrorMessage()
                )
            );
        }
    }
}
```

**Résultat** : Dashboard en temps réel + alertes automatiques !

### 3. Audit et compliance

Tracer toutes les opérations pour audit et conformité réglementaire.

```java
@Component
public class AuditHandler implements ConsumerHandler {
    
    private final AuditRepository auditRepository;
    
    @FunctionalHandler(eventType = HandlerConsumedEvent.class)
    public void auditSuccess(HandlerConsumedEvent event) {
        AuditLog log = AuditLog.builder()
            .eventType(event.getOriginalEventType())
            .handler(event.getHandlerName())
            .status("SUCCESS")
            .timestamp(event.getOccurredOn())
            .eventId(event.getOriginalEventId())
            .build();
        
        auditRepository.save(log);
    }
    
    @FunctionalHandler(eventType = HandlerFailedEvent.class)
    public void auditFailure(HandlerFailedEvent event) {
        AuditLog log = AuditLog.builder()
            .eventType(event.getOriginalEventType())
            .handler(event.getHandlerName())
            .status("FAILURE")
            .errorMessage(event.getErrorMessage())
            .attemptNumber(event.getAttemptNumber())
            .timestamp(event.getOccurredOn())
            .eventId(event.getOriginalEventId())
            .build();
        
        auditRepository.save(log);
    }
}
```

**Résultat** : Audit trail complet pour conformité RGPD/SOC2 !

### 4. Retry intelligent

Gérer les retries avec logique métier personnalisée.

```java
@Component
public class SmartRetryHandler implements ConsumerHandler {
    
    @FunctionalHandler(eventType = HandlerFailedEvent.class)
    public void handleSmartRetry(HandlerFailedEvent event) {
        // Ne pas retenter si c'est une erreur de validation
        if (event.getExceptionClass().contains("ValidationException")) {
            log.warn("Validation error, skipping retry: {}", event.getErrorMessage());
            return;
        }
        
        // Augmenter le délai entre retries pour les timeouts
        if (event.getExceptionClass().contains("TimeoutException")) {
            int delayMinutes = event.getAttemptNumber() * 5; // 5min, 10min, 15min
            scheduleRetry(event.getOriginalEventId(), delayMinutes);
        }
        
        // Alerter l'équipe si échec répété
        if (event.getAttemptNumber() >= 2) {
            notifyTeam(event);
        }
    }
}
```

### 5. Statistiques métier

Calculer des métriques métier automatiquement.

```java
@Component
public class BusinessMetricsHandler implements ConsumerHandler {
    
    @FunctionalHandler(eventType = HandlerConsumedEvent.class)
    public void calculateMetrics(HandlerConsumedEvent event) {
        // Calculer le temps de traitement
        Duration processingTime = Duration.between(
            getOriginalEventTime(event.getOriginalEventId()),
            event.getOccurredOn()
        );
        
        metricsService.recordProcessingTime(
            event.getOriginalEventType(),
            event.getHandlerName(),
            processingTime
        );
        
        // Incrémenter compteurs métier
        if ("PaymentMadeEvent".equals(event.getOriginalEventType())) {
            metricsService.incrementTotalPaymentsProcessed();
        }
        
        if ("DealCreatedEvent".equals(event.getOriginalEventType())) {
            metricsService.incrementTotalDealsCreated();
        }
    }
}
```

---

## 📊 Exploitation des données

### Requêtes SQL utiles

```sql
-- 1. Taux de succès par type d'événement
SELECT 
    payload::json->>'originalEventType' as event_type,
    COUNT(*) as total_success
FROM event_record 
WHERE event_type = 'HandlerConsumedEvent'
GROUP BY event_type
ORDER BY total_success DESC;

-- 2. Handlers les plus lents
SELECT 
    payload::json->>'handlerName' as handler,
    AVG(EXTRACT(EPOCH FROM (occurred_on - 
        (SELECT occurred_on FROM event_record e2 
         WHERE e2.event_id = (payload::json->>'originalEventId')::uuid)
    ))) as avg_seconds
FROM event_record
WHERE event_type = 'HandlerConsumedEvent'
GROUP BY handler
ORDER BY avg_seconds DESC;

-- 3. Événements avec échecs multiples
SELECT 
    payload::json->>'originalEventId' as original_event,
    payload::json->>'originalEventType' as event_type,
    COUNT(*) as failure_count
FROM event_record
WHERE event_type = 'HandlerFailedEvent'
GROUP BY original_event, event_type
HAVING COUNT(*) > 1
ORDER BY failure_count DESC;

-- 4. Timeline complète d'un événement
SELECT 
    event_type,
    occurred_on,
    CASE 
        WHEN event_type = 'HandlerConsumedEvent' 
            THEN 'SUCCESS by ' || payload::json->>'handlerName'
        WHEN event_type = 'HandlerFailedEvent' 
            THEN 'FAILURE: ' || payload::json->>'errorMessage'
        ELSE 'Original event'
    END as status
FROM event_record
WHERE event_id = 'uuid-original'
   OR payload::json->>'originalEventId' = 'uuid-original'
ORDER BY occurred_on;

-- 5. Handlers avec le plus d'échecs finaux
SELECT 
    payload::json->>'handlerName' as handler,
    payload::json->>'originalEventType' as event_type,
    COUNT(*) as permanent_failures
FROM event_record
WHERE event_type = 'HandlerFailedEvent'
AND payload::json->>'isFinalFailure' = 'true'
GROUP BY handler, event_type
ORDER BY permanent_failures DESC;
```

---

## ✅ Checklist pour les développeurs

### Lors de la création d'un handler

- [ ] Créer la classe handler implémentant `ConsumerHandler`
- [ ] Annoter la méthode avec `@FunctionalHandler`
- [ ] Implémenter la logique métier
- [ ] Gérer les exceptions (relancer si retry souhaité)
- [ ] ✅ **RIEN d'autre !** Les événements de confirmation sont automatiques

### Ce que vous N'avez PAS à faire

- [ ] ❌ Publier manuellement `HandlerConsumedEvent`
- [ ] ❌ Publier manuellement `HandlerFailedEvent`
- [ ] ❌ Gérer le statut de l'événement en BD
- [ ] ❌ Logger manuellement le succès/échec (déjà fait)

### Exemple complet correct

```java
@Component
@RequiredArgsConstructor
public class OrderHandler implements ConsumerHandler {
    
    private final OrderService orderService;
    
    @FunctionalHandler(eventType = OrderCreatedEvent.class, maxAttempts = 3)
    public void handleOrderCreated(OrderCreatedEvent event) {
        // ✅ Votre logique métier UNIQUEMENT
        orderService.processOrder(event.getOrderUuid());
        
        // ✅ C'EST TOUT ! Le reste est automatique :
        // - HandlerConsumedEvent publié si succès
        // - HandlerFailedEvent publié si exception
        // - Retry automatique si échec
        // - Statut BD mis à jour
    }
}
```

---

## 🎉 Avantages de cette règle

| Avantage | Détail |
|----------|--------|
| 🔍 **Traçabilité** | Chaque handler laisse une trace automatiquement |
| 🔗 **Workflows** | Créer des processus réactifs facilement |
| 📊 **Monitoring** | Métriques automatiques sans code boilerplate |
| 🔔 **Alerting** | Détection immédiate des problèmes |
| 📝 **Audit** | Conformité réglementaire garantie |
| 🎯 **Simplicité** | Le développeur code uniquement la logique métier |
| 🔄 **Cohérence** | Tous les handlers suivent la même règle |
| 📈 **Analytics** | Statistiques métier automatiques |

---

## 🚀 Conclusion

Cette règle transforme le système d'événements en une infrastructure complète pour :
- ✅ Orchestrer des workflows complexes
- ✅ Monitorer la santé du système
- ✅ Auditer toutes les opérations
- ✅ Gérer intelligemment les erreurs
- ✅ Calculer des métriques métier

**Et tout ça SANS code boilerplate dans les handlers !**

---

**Date** : 4 mars 2026  
**Version** : 1.1.0  
**Status** : ✅ Règle implémentée et obligatoire

