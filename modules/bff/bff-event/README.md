# Module BFF-EVENT - Gestion des événements (Partie gauche de l'hexagone)

## 🎯 Description

Module contenant la définition des événements du domaine et des **handlers d'événements**.

⚠️ **ARCHITECTURE HEXAGONALE** : Ce module fait partie de la **PARTIE GAUCHE** (adaptateurs d'entrée pour les événements).

Ce module est indépendant de toute implémentation technique de persistence et peut être réutilisé avec différents systèmes de messaging (base de données, Kafka, RabbitMQ, etc.).

## 📐 Position dans l'architecture hexagonale

```
┌─────────────────────────────────────────────┐
│            PARTIE GAUCHE                    │
│      (Adaptateurs d'entrée)                 │
├─────────────────────────────────────────────┤
│  bff-api (HTTP)      bff-event (Événements)│
│                             ↓                │
│                      ✅ Handlers ici        │
└──────────────────┬──────────────────────────┘
                   ↓
┌──────────────────────────────────────────────┐
│          HEXAGONE CENTRAL (bff-core)         │
│          ↓ Appelle les Services métier       │
└──────────────────┬──────────────────────────┘
                   ↓
┌──────────────────────────────────────────────┐
│            PARTIE DROITE                     │
│      (Adaptateurs de sortie)                 │
├──────────────────────────────────────────────┤
│  bff-provider           bff-event-dispatcher │
│  (Persistence)          (Event Store)        │
└──────────────────────────────────────────────┘
```

## 🔗 Dépendances Maven

### ✅ Dépendances AUTORISÉES

```xml
<dependencies>
    <!-- ✅ bff-core : Pour appeler les Services métier -->
    <dependency>
        <groupId>com.ulr.paytogether</groupId>
        <artifactId>bff-core</artifactId>
    </dependency>

    <!-- ✅ bff-event-dispatcher : Pour l'implémentation technique -->
    <dependency>
        <groupId>com.ulr.paytogether</groupId>
        <artifactId>bff-event-dispatcher</artifactId>
    </dependency>
</dependencies>
```

### ❌ Dépendances INTERDITES

```xml
<!-- ❌ NE JAMAIS ajouter ces dépendances -->
<dependency>
    <artifactId>bff-provider</artifactId> <!-- ❌ INTERDIT -->
</dependency>
<dependency>
    <artifactId>bff-api</artifactId> <!-- ❌ INTERDIT -->
</dependency>
<dependency>
    <artifactId>spring-boot-starter-data-jpa</artifactId> <!-- ❌ INTERDIT -->
</dependency>
```

## 🚨 Règles CRITIQUES

### ✅ À FAIRE dans les handlers

1. **Injecter UNIQUEMENT** des Services du core (suffixe `Service`)
2. **Utiliser** les modèles métier du core (suffixe `Modele`)
3. **Implémenter** `ConsumerHandler`
4. **Annoter** avec `@Component` et `@FunctionalHandler`

### ❌ À NE JAMAIS FAIRE

1. ❌ **Ne JAMAIS injecter** de Repository (suffixe `Repository`)
2. ❌ **Ne JAMAIS injecter** de Provider (suffixe `Provider`)
3. ❌ **Ne JAMAIS créer** d'entité JPA (suffixe `Jpa`)
4. ❌ **Ne JAMAIS importer** de classes de `bff-provider`

### Exemple CORRECT vs INCORRECT

#### ❌ INCORRECT
```java
@Component
public class AccountValidationHandler {
    private final ValidationTokenRepository tokenRepository; // ❌ ACCÈS DIRECT !
    
    public void handle(AccountValidationEvent event) {
        ValidationTokenJpa tokenJpa = ...; // ❌ Entité JPA !
        tokenRepository.save(tokenJpa); // ❌ Repository !
    }
}
```

#### ✅ CORRECT
```java
@Component
@RequiredArgsConstructor
public class AccountValidationHandler implements ConsumerHandler {
    private final ValidationTokenService validationTokenService; // ✅ Service du core
    
    @FunctionalHandler(
        eventType = AccountValidationEvent.class,
        maxAttempts = 3,
        description = "Envoie un email de validation"
    )
    public void handleAccountValidation(AccountValidationEvent event) {
        ValidationTokenModele tokenModele = ...; // ✅ Modèle métier
        validationTokenService.creer(tokenModele); // ✅ Service métier
        
        // ✅ Retry automatique hérité de ConsumerHandler (Spring @Retryable)
    }
}
```

## 🔄 Stratégie de retry automatique (Spring @Retryable)

### Configuration par défaut

Tous les handlers héritent automatiquement d'une stratégie de retry via l'interface `ConsumerHandler` :

```java
@Retryable(
    retryFor = Exception.class,
    maxAttempts = 3,
    backoff = @Backoff(
        delay = 1000,           // 1 seconde
        multiplier = 1.5,       // Facteur 1.5 (backoff exponentiel)
        maxDelay = 30000,       // Max 30 secondes
        random = true           // Jitter activé
    )
)
public interface ConsumerHandler {
    // Tous les handlers héritent du retry automatiquement
}
```

### Avantages

- ✅ **Configuration centralisée** : une seule définition pour tous les handlers
- ✅ **Standard Spring** : utilise Spring Retry (pas de code custom)
- ✅ **Backoff exponentiel** : 1s → 1.5s → 2.25s → etc.
- ✅ **Jitter** : évite les collisions lors de retries simultanés
- ✅ **Pas de boilerplate** : pas besoin d'annoter chaque méthode

### Séquence de retry

1. **Tentative 1** : immédiat (0 ms)
2. **Tentative 2** : ~1000 ms (1 sec + jitter aléatoire)
3. **Tentative 3** : ~1500 ms (1.5 sec + jitter aléatoire)

### Configuration personnalisée (optionnelle)

Si un handler spécifique nécessite une stratégie différente, vous pouvez surcharger `@Retryable` :

```java
@Component
@RequiredArgsConstructor
public class CriticalPaymentHandler implements ConsumerHandler {
    private final PaymentService paymentService;
    
    @FunctionalHandler(
        eventType = PaymentInitiatedEvent.class,
        maxAttempts = 5
    )
    @Retryable(
        retryFor = Exception.class,
        maxAttempts = 5,
        backoff = @Backoff(
            delay = 2000,        // 2 secondes
            multiplier = 2.0,    // Double à chaque tentative
            maxDelay = 60000     // Max 1 minute
        )
    )
    public void handlePayment(PaymentInitiatedEvent event) {
        // Configuration personnalisée pour ce handler critique
        paymentService.process(event);
    }
}
```

### Activation de @EnableRetry

Le support de `@Retryable` est activé via la classe de configuration :

```java
@Configuration
@EnableRetry
public class EventConfiguration {
    // Active Spring Retry pour tous les handlers
}
```

## Responsabilités

- Définir les événements du domaine (classes héritant de `DomainEvent`)
- Définir les handlers d'événements (classes implémentant `ConsumerHandler`)
- Fournir l'annotation `@FunctionalHandler` pour marquer les méthodes handler
- Définir l'interface `EventDispatcher` pour la publication d'événements

## Structure

```
bff-event/
├── model/              # Événements du domaine
│   ├── DomainEvent.java          # Classe abstraite de base
│   ├── EventDispatcher.java      # Interface dispatcher
│   └── PaymentMadeEvent.java     # Exemple d'événement
├── handler/            # Handlers d'événements
│   ├── ConsumerHandler.java      # Interface de base
│   └── PaymentHandler.java       # Exemple de handler
└── annotation/         # Annotations
    └── FunctionalHandler.java    # Annotation pour handlers
```

## Utilisation

### Créer un événement

```java
@Getter
@NoArgsConstructor
public class DealCreatedEvent extends DomainEvent {
    
    private UUID dealUuid;
    private String titre;
    private BigDecimal prixDeal;
    
    public DealCreatedEvent(UUID dealUuid, String titre, BigDecimal prixDeal) {
        super("DealService");
        this.dealUuid = dealUuid;
        this.titre = titre;
        this.prixDeal = prixDeal;
    }
    
    @Override
    public String toJson() {
        // Sérialisation JSON
    }
}
```

### Créer un handler

```java
@Component
@RequiredArgsConstructor
public class DealHandler implements ConsumerHandler {
    
    @FunctionalHandler(
        eventType = DealCreatedEvent.class,
        maxAttempts = 3,
        description = "Traite la création d'un deal"
    )
    public void handleDealCreated(DealCreatedEvent event) {
        // Logique métier
    }
}
```

## Dépendances

- Spring Boot Starter
- Lombok
- Jackson (JSON)

## Convention de nommage

- Événements : Suffixe `Event` (ex: `DealCreatedEvent`)
- Handlers : Suffixe `Handler` (ex: `DealHandler`)
- Méthodes handler : Préfixe `handle` (ex: `handleDealCreated`)

## Version

1.0.0

