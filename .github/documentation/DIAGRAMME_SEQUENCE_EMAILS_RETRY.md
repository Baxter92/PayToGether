# Diagramme de séquence : Avant/Après correction des emails

## ❌ AVANT : Doublons d'emails lors des retries

```
┌─────────┐     ┌─────────────┐     ┌──────────────┐     ┌──────────┐
│ Service │     │   Handler   │     │ EmailService │     │ Database │
└────┬────┘     └──────┬──────┘     └──────┬───────┘     └────┬─────┘
     │                 │                    │                   │
     │ Publish Event   │                    │                   │
     ├────────────────>│                    │                   │
     │                 │                    │                   │
     │                 │ TENTATIVE 1        │                   │
     │                 ├───────────────────>│                   │
     │                 │ Envoyer email      │                   │
     │                 │<───────────────────│                   │
     │                 │ ✅ Email envoyé    │                   │
     │                 │                    │                   │
     │                 │                         Opération DB   │
     │                 ├─────────────────────────────────────> │
     │                 │                                        │
     │                 │<───────────────────────────────────────│
     │                 │ ❌ ERREUR Database                     │
     │                 │                                        │
     │                 │ ⏰ RETRY (1 seconde)                   │
     │                 │                                        │
     │                 │ TENTATIVE 2                            │
     │                 ├───────────────────>│                   │
     │                 │ Envoyer email      │                   │
     │                 │<───────────────────│                   │
     │                 │ ✅ Email envoyé    │ ⚠️ DOUBLON !     │
     │                 │                    │                   │
     │                 │                         Opération DB   │
     │                 ├─────────────────────────────────────> │
     │                 │                                        │
     │                 │<───────────────────────────────────────│
     │                 │ ✅ Succès                              │
     │<────────────────│                                        │
     │ Event handled   │                                        │
     │                 │                                        │
     
📧 RÉSULTAT : 2 emails identiques reçus par l'utilisateur
```

## ✅ APRÈS : 1 seul email après succès

```
┌─────────┐     ┌─────────────┐     ┌──────────────┐     ┌──────────┐
│ Service │     │   Handler   │     │ EmailService │     │ Database │
└────┬────┘     └──────┬──────┘     └──────┬───────┘     └────┬─────┘
     │                 │                    │                   │
     │ Publish Event   │                    │                   │
     ├────────────────>│                    │                   │
     │                 │                    │                   │
     │                 │ TENTATIVE 1        │                   │
     │                 │                         Opération DB   │
     │                 ├─────────────────────────────────────> │
     │                 │                                        │
     │                 │<───────────────────────────────────────│
     │                 │ ❌ ERREUR Database                     │
     │                 │                                        │
     │                 │ ⏰ RETRY (1 seconde)                   │
     │                 │                                        │
     │                 │ TENTATIVE 2                            │
     │                 │                         Opération DB   │
     │                 ├─────────────────────────────────────> │
     │                 │                                        │
     │                 │<───────────────────────────────────────│
     │                 │ ✅ Succès                              │
     │                 │                                        │
     │                 │ Envoyer email                          │
     │                 ├───────────────────>│                   │
     │                 │                    │                   │
     │                 │<───────────────────│                   │
     │                 │ ✅ Email envoyé    │ ✅ 1 SEUL EMAIL  │
     │<────────────────│                    │                   │
     │ Event handled   │                    │                   │
     │                 │                    │                   │
     
📧 RÉSULTAT : 1 seul email reçu par l'utilisateur
```

## 🔄 Séquence de retry avec backoff exponentiel

```
Tentative 1 : t = 0 ms      (immédiat)
     ↓
   Échec ❌
     ↓
Tentative 2 : t = ~1000 ms  (1 sec + jitter)
     ↓
   Échec ❌
     ↓
Tentative 3 : t = ~1500 ms  (1.5 sec + jitter)
     ↓
   Succès ✅
     ↓
Email envoyé 1 seule fois 📧
```

## 💡 Cas d'usage : PaymentRefundedHandler

### ❌ AVANT

```java
@FunctionalHandler(maxAttempts = 3)
public void handlePaymentRefunded(PaymentRefundedEvent event) {
    try {
        // 1. Envoyer email EN PREMIER
        emailNotificationService.envoyerNotification(...);
        log.info("✅ Email envoyé");

        // 2. Supprimer participation
        squarePaymentService.supprimerParticipation(...);
        log.info("✅ Participation supprimée");

    } catch (Exception e) {
        log.error("❌ Erreur: {}", e.getMessage());
        throw e; // ⚠️ Retry TOUT le handler → Email renvoyé
    }
}
```

**Problème** :
- Si `supprimerParticipation()` échoue à la tentative 1
- Le retry relance TOUT le handler
- L'email est renvoyé à chaque tentative

### ✅ APRÈS

```java
@FunctionalHandler(maxAttempts = 3)
public void handlePaymentRefunded(PaymentRefundedEvent event) {
    try {
        // 1. Supprimer participation EN PREMIER (opération critique)
        squarePaymentService.supprimerParticipation(...);
        log.info("✅ Participation supprimée");

        // 2. Envoyer email UNIQUEMENT après succès
        envoyerEmailRemboursement(event);

    } catch (Exception e) {
        log.error("❌ Erreur: {}", e.getMessage());
        throw e; // ✅ Retry uniquement l'opération critique
    }
}

/**
 * Méthode isolée qui ne propage PAS les exceptions
 */
private void envoyerEmailRemboursement(PaymentRefundedEvent event) {
    try {
        emailNotificationService.envoyerNotification(...);
        log.info("✅ Email envoyé");
    } catch (Exception e) {
        // ⚠️ Log uniquement, pas de throw
        log.error("⚠️ Échec email: {}", e.getMessage());
    }
}
```

**Avantages** :
1. ✅ Email envoyé 1 seule fois après succès de l'opération
2. ✅ Si opération échoue, retry sans renvoyer l'email
3. ✅ Si email échoue, pas de blocage du handler

## 📊 Timeline détaillée d'un refund avec retry

```
t = 0 ms
├─ Handler déclenché
├─ TENTATIVE 1
│  ├─ supprimerParticipation() → ❌ Connection timeout
│  └─ Exception propagée → Préparation retry
│
t = ~1000 ms (+ jitter aléatoire)
├─ TENTATIVE 2
│  ├─ supprimerParticipation() → ❌ Connection timeout
│  └─ Exception propagée → Préparation retry
│
t = ~2500 ms (+ jitter aléatoire)
├─ TENTATIVE 3
│  ├─ supprimerParticipation() → ✅ Succès
│  ├─ envoyerEmailRemboursement() → ✅ Email envoyé
│  └─ Handler réussi
│
Résultat : 1 seul email envoyé à t = 2500 ms
```

## 🎯 Règles de conception

### ✅ À FAIRE

```
1. Opérations critiques EN PREMIER
   ↓
2. Retry sur les opérations critiques
   ↓
3. Email EN DERNIER après succès
   ↓
4. Méthode d'envoi isolée
   ↓
5. Exception d'email NON propagée
```

### ❌ À ÉVITER

```
1. Email AVANT opération critique
   ↓
2. Exception d'email propagée
   ↓
3. Retry global incluant l'email
   ↓
4. Pas de logs explicites
```

---

**Documentation complète** : `.github/documentation/CORRECTION_ENVOI_EMAILS_RETRY.md`

