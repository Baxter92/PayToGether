# Guide des versions du SDK Square Java

## Version utilisée : 46.0.0.20260122

Le projet PayToGether utilise la version **46.0.0.20260122** du Square Java SDK.

### Structure des packages

Le SDK Square 46.0.0 utilise la structure de packages suivante :

- **`com.squareup.square.SquareClient`** : Client principal
- **`com.squareup.square.types.*`** : Tous les types (modèles, requêtes, réponses)
- **`com.squareup.square.core.SquareApiException`** : Exception API
- **`com.squareup.square.core.Environment`** : Environnements (SANDBOX, PRODUCTION)

### Exemple d'utilisation

```java
import com.squareup.square.SquareClient;
import com.squareup.square.core.SquareApiException;
import com.squareup.square.types.*;

// Créer le client
SquareClient client = SquareClient.builder()
    .token("YOUR_ACCESS_TOKEN")
    .build();

// Créer un paiement
CreatePaymentRequest request = CreatePaymentRequest.builder()
    .sourceId("CARD_NONCE")
    .idempotencyKey(UUID.randomUUID().toString())
    .amountMoney(Money.builder()
        .amount(1000L)
        .currency(Currency.CAD)
        .build())
    .build();

try {
    CreatePaymentResponse response = client.payments().create(request);
    String paymentId = response.getPayment()
        .flatMap(Payment::getId)
        .orElseThrow();
} catch (SquareApiException e) {
    log.error("Erreur Square: {}", e.getMessage());
}
```

### Points importants

1. **Optional partout** : La plupart des getters retournent des `Optional<T>`
2. **Builder pattern** : Tous les objets utilisent le pattern Builder
3. **Méthodes API** : Accès direct via `client.payments()`, `client.refunds()`, etc.
4. **Pas de sous-packages** : Tout est dans `com.squareup.square.types.*`

### Dépendance Maven

```xml
<dependency>
    <groupId>com.squareup</groupId>
    <artifactId>square</artifactId>
    <version>46.0.0.20260122</version>
</dependency>
```

### Documentation officielle

- GitHub : https://github.com/square/square-java-sdk
- Maven Central : https://central.sonatype.com/artifact/com.squareup/square

---

**Dernière mise à jour** : 4 mars 2026

