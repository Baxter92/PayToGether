# 📧 Système de Notification avec EventDispatcher - PayToGether

## ✅ Statut : IMPLÉMENTÉ

Le système de notification a été **réimplémenté** en utilisant le **pattern EventDispatcher** existant dans le projet.

---

## 🎯 Changements par rapport à l'implémentation précédente

### ❌ Ancienne approche (supprimée)
- Envoi direct et asynchrone avec `@Async`
- NotificationService avec NotificationProvider
- Notifications déclenchées directement depuis les ApiAdapters

### ✅ Nouvelle approche (EventDispatcher)
- **Découplage total** : Les services métier ne dépendent que d'EventDispatcher
- **Asynchrone par design** : Les événements sont traités en arrière-plan
- **Résilience** : Retry automatique (max 3 tentatives)
- **Traçabilité** : Tous les événements sont enregistrés en BDD (`event_record`)

---

## 🏗️ Architecture mise en place

```
UtilisateurService.creer()
    ↓
1. Créer l'utilisateur en BDD
    ↓
2. Générer token de validation (24h)
    ↓
3. Créer AccountValidationEvent
    ↓
4. eventDispatcher.dispatchAsync(event)
    ↓
5. Événement enregistré en BDD (status: PENDING)
    ↓
6. EventConsumerService traite l'événement
    ↓
7. AccountValidationHandler.handleAccountValidation()
    ↓
8. Sauvegarde token en BDD (validation_token)
    ↓
9. EmailService.envoyerEmail() avec template Thymeleaf
    ↓
10. Événement marqué PROCESSED ou FAILED
```

---

## 📦 Fichiers créés

### bff-event (Événements métier) - 5 fichiers
1. ✅ `AccountValidationEvent.java` - Validation de compte avec token 24h
2. ✅ `PaymentSuccessfulNotificationEvent.java` - Notification de paiement réussi
3. ✅ `PaymentFailedNotificationEvent.java` - Notification de paiement échoué
4. ✅ `PasswordResetEvent.java` - Réinitialisation de mot de passe
5. ✅ `DealCreatedEvent.java` - Notification de création de deal (marchand)
6. ✅ `AccountActivationEvent.java` - Notification d'activation de compte

### bff-api (Handlers) - 1 fichier
7. ✅ `AccountValidationHandler.java` - Handler pour AccountValidationEvent
   - Sauvegarde du token en BDD
   - Envoi d'email avec template personnalisé
   - Retry automatique en cas d'échec (max 3)

### bff-core (Services modifiés) - 1 fichier
8. ✅ `UtilisateurServiceImpl.java` - Modifié pour dispatcher AccountValidationEvent
   - Injection d'EventDispatcher
   - Génération de token unique
   - Dispatch asynchrone de l'événement

### bff-provider (Inchangé)
- `EmailService.java` - Déjà créé précédemment
- `ValidationTokenRepository.java` - Déjà créé précédemment
- `ValidationTokenJpa.java` - Déjà créé précédemment

### bff-configuration (Templates HTML)
- Les 13 templates HTML créés précédemment sont conservés

---

## 📊 Fichiers supprimés (ancienne implémentation)

### bff-core
- ❌ `NotificationService.java` (interface)
- ❌ `NotificationServiceImpl.java` (implémentation)
- ❌ `NotificationProvider.java` (interface)
- ❌ `NotificationEmailModele.java` (modèle)
- ❌ `TypeNotificationEmail.java` (énumération)

### bff-provider
- ❌ `NotificationProviderAdapter.java` (adapter)

### bff-configuration
- ❌ `AsyncConfiguration.java` (configuration @Async)

### bff-api
- ❌ `ValidationResource.java` (endpoints de validation)

### bff-api (nettoyé)
- ✅ `UtilisateurApiAdapter.java` - Nettoyé (suppression des appels directs à NotificationService)

---

## 🎯 Événements implémentés

| Événement | Description | Propriétés | Status |
|-----------|-------------|-----------|--------|
| `AccountValidationEvent` | Validation de compte | utilisateurUuid, email, prenom, nom, token, dateExpiration | ✅ Créé |
| `PaymentSuccessfulNotificationEvent` | Paiement réussi | utilisateurUuid, email, montant, methodePaiement, titreDeal | ✅ Créé |
| `PaymentFailedNotificationEvent` | Paiement échoué | utilisateurUuid, email, montant, raisonEchec, titreDeal | ✅ Créé |
| `PasswordResetEvent` | Réinitialisation MDP | utilisateurUuid, email, prenom, nom, token, dateExpiration | ✅ Créé |
| `DealCreatedEvent` | Deal créé (marchand) | dealUuid, marchandUuid, emailMarchand, titreDeal, montant | ✅ Créé |
| `AccountActivationEvent` | Activation de compte | utilisateurUuid, email, prenom, nom, dateActivation | ✅ Créé |

### À implémenter (selon les besoins)
- `PaymentReminderEvent` - Rappel de paiement
- `AccountUpdateNotificationEvent` - Mise à jour de compte
- `AccountDeactivationEvent` - Désactivation de compte
- `DealValidatedEvent` - Deal validé (marchand)
- `DealCancelledEvent` - Deal annulé (marchand)
- `NewParticipantEvent` - Nouveau participant à un deal
- `PayoutCompletedEvent` - Payout effectué (marchand)

---

## 🚀 Utilisation

### Exemple dans un service métier

```java
@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {
    
    private final UtilisateurProvider utilisateurProvider;
    private final EventDispatcher eventDispatcher;
    
    @Override
    public UtilisateurModele creer(UtilisateurModele utilisateur) {
        // 1. Créer l'utilisateur
        UtilisateurModele cree = utilisateurProvider.sauvegarder(utilisateur);
        
        // 2. Générer token
        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiration = LocalDateTime.now().plusHours(24);
        
        // 3. Créer et dispatcher l'événement
        AccountValidationEvent event = new AccountValidationEvent(
            cree.getUuid(),
            cree.getEmail(),
            cree.getPrenom(),
            cree.getNom(),
            token,
            expiration
        );
        
        eventDispatcher.dispatchAsync(event);
        
        return cree;
    }
}
```

### Exemple de handler

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountValidationHandler implements ConsumerHandler {
    
    private final EmailService emailService;
    private final ValidationTokenRepository tokenRepository;
    
    @FunctionalHandler(
        eventType = AccountValidationEvent.class,
        maxAttempts = 3,
        description = "Envoie un email de validation de compte avec token 24h"
    )
    public void handleAccountValidation(AccountValidationEvent event) {
        // 1. Sauvegarder le token
        ValidationTokenJpa tokenJpa = ValidationTokenJpa.builder()
                .token(event.getToken())
                .utilisateurUuid(event.getUtilisateurUuid())
                .dateExpiration(event.getDateExpiration())
                .typeToken("VALIDATION_COMPTE")
                .build();
        tokenRepository.save(tokenJpa);
        
        // 2. Envoyer l'email
        Map<String, Object> variables = Map.of(
            "prenom", event.getPrenom(),
            "nom", event.getNom(),
            "token", event.getToken(),
            "lienValidation", "https://dev.dealtogether.ca/validation-compte?token=" + event.getToken()
        );
        
        emailService.envoyerEmail(
            event.getEmail(),
            "Validation de votre compte PayToGether",
            "notification-account-validation",
            variables
        );
    }
}
```

---

## 📋 Checklist pour ajouter une nouvelle notification

1. **Créer l'événement** (bff-event/model)
   - [ ] Hériter de `DomainEvent`
   - [ ] Ajouter propriétés nécessaires
   - [ ] Annoter avec `@JsonCreator` et `@JsonProperty`
   - [ ] Implémenter `toJson()`

2. **Dispatcher l'événement** (Service métier)
   - [ ] Injecter `EventDispatcher`
   - [ ] Créer l'événement après l'action métier
   - [ ] Utiliser `dispatchAsync(event)`

3. **Créer le handler** (bff-api/handler)
   - [ ] Implémenter `ConsumerHandler`
   - [ ] Annoter la méthode avec `@FunctionalHandler`
   - [ ] Gérer les erreurs (propagation pour retry)

4. **Créer le template HTML** (bff-configuration/resources/templates/notifications/)
   - [ ] Design professionnel
   - [ ] Variables dynamiques Thymeleaf
   - [ ] Responsive design

5. **Tester**
   - [ ] Créer un utilisateur via API
   - [ ] Vérifier l'événement dans `event_record`
   - [ ] Vérifier l'email envoyé
   - [ ] Vérifier les retry en cas d'échec

---

## 📊 Traçabilité

Tous les événements sont enregistrés dans `event_record` :
- `event_id` : UUID unique
- `event_type` : AccountValidationEvent, PaymentSuccessfulNotificationEvent, etc.
- `payload` : JSON de l'événement
- `status` : PENDING → PROCESSING → PROCESSED ou FAILED
- `attempts` : Nombre de tentatives (max 3)
- `processed_at` : Date de traitement

---

## 🎯 Avantages de l'approche EventDispatcher

### ✅ Découplage
- Les services métier ne dépendent que d'EventDispatcher
- Pas de dépendance directe à EmailService
- Facile à tester avec un mock d'EventDispatcher

### ✅ Asynchrone
- Les événements sont traités en arrière-plan
- Aucun blocage du thread principal
- Performance optimale

### ✅ Résilience
- Retry automatique en cas d'échec (max 3 tentatives)
- Pas de perte de notification même en cas d'erreur temporaire
- Traçabilité complète en BDD

### ✅ Extensibilité
- Facile d'ajouter de nouveaux handlers
- Plusieurs handlers peuvent écouter le même événement
- Possibilité d'ajouter des handlers pour audit, analytics, etc.

---

## 🔄 Prochaines étapes

### Priorité HAUTE 🔴
- [ ] Créer les handlers manquants (PaymentSuccessful, PaymentFailed, etc.)
- [ ] Créer les événements pour les deals (DealValidated, DealCancelled, etc.)
- [ ] Dispatcher les événements depuis DealService
- [ ] Dispatcher les événements depuis PaiementService

### Priorité MOYENNE 🟠
- [ ] Créer endpoint de validation de compte (GET /api/validation/compte?token=xxx)
- [ ] Implémenter le système de réinitialisation de mot de passe
- [ ] Ajouter notification de mise à jour de compte
- [ ] Ajouter notification d'activation/désactivation

### Priorité BASSE 🟢
- [ ] Ajouter tests unitaires pour les handlers
- [ ] Ajouter tests d'intégration
- [ ] Créer dashboard admin pour voir les événements
- [ ] Ajouter métriques et monitoring

---

## 📝 Exemple complet

### 1. Inscription utilisateur
```http
POST /api/auth/register
Content-Type: application/json

{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@example.com",
  "motDePasse": "MotDePasse123!",
  "role": "CLIENT"
}
```

### 2. Flux d'événement
```
1. AuthResource.register() appelle UtilisateurApiAdapter.creer()
2. UtilisateurApiAdapter.creer() appelle UtilisateurService.creer()
3. UtilisateurService.creer() :
   - Sauvegarde l'utilisateur
   - Génère un token
   - Crée AccountValidationEvent
   - Dispatche l'événement : eventDispatcher.dispatchAsync(event)
4. EventDispatcher enregistre l'événement en BDD (status: PENDING)
5. EventConsumerService lit les événements PENDING
6. EventConsumerService appelle AccountValidationHandler.handleAccountValidation()
7. AccountValidationHandler :
   - Sauvegarde le token en BDD
   - Envoie l'email via EmailService
8. Événement marqué PROCESSED
```

### 3. Vérification en BDD
```sql
-- Vérifier l'événement
SELECT * FROM event_record WHERE event_type = 'AccountValidationEvent';

-- Vérifier le token
SELECT * FROM validation_token WHERE utilisateur_uuid = 'xxx';
```

---

**Date d'implémentation** : 4 mars 2026  
**Statut** : ✅ **TERMINÉ - Pattern EventDispatcher**  
**Prochaine étape** : Créer les handlers manquants et dispatcher les événements depuis les autres services

