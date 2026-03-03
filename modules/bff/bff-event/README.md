# Module BFF-EVENT

## Description

Module contenant la définition des événements du domaine et des handlers.
Ce module est indépendant de toute implémentation technique et peut être réutilisé avec différents systèmes de messaging (base de données, Kafka, RabbitMQ, etc.).

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

