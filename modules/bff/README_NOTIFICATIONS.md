# 📧 Système de Notification par Email - PayToGether

## 🎯 Vue d'ensemble

Le système de notification par email de PayToGether permet d'envoyer des emails personnalisés aux utilisateurs de manière **asynchrone** pour différents événements (validation de compte, paiements, deals, etc.).

## 🏗️ Architecture

### Modules impliqués

```
bff-core/
├── enumeration/
│   └── TypeNotificationEmail.java
├── modele/
│   └── NotificationEmailModele.java
├── provider/
│   └── NotificationProvider.java (interface)
└── domaine/
    ├── service/
    │   └── NotificationService.java (interface)
    └── impl/
        └── NotificationServiceImpl.java

bff-provider/
├── adapter/
│   ├── entity/
│   │   └── ValidationTokenJpa.java
│   └── NotificationProviderAdapter.java
├── repository/
│   └── ValidationTokenRepository.java
└── service/
    └── EmailService.java

bff-configuration/
└── resources/
    └── templates/
        └── notifications/
            ├── notification-validation-compte.html
            ├── notification-paiement-reussi.html
            ├── notification-paiement-echoue.html
            ├── notification-reinitialisation-mot-de-passe.html
            ├── notification-activation-compte.html
            ├── notification-desactivation-compte.html
            ├── notification-mise-a-jour-compte.html
            ├── notification-rappel-paiement.html
            ├── notification-deal-cree.html
            ├── notification-deal-valide.html
            ├── notification-deal-annule.html
            ├── notification-nouveau-participant.html
            └── notification-payout-deal.html

bff-api/
├── resource/
│   └── ValidationResource.java
└── apiadapter/
    └── UtilisateurApiAdapter.java (modifié)
```

## 📋 Types de notifications

### 1. **VALIDATION_COMPTE**
- **Quand** : Lors de l'inscription d'un utilisateur
- **Contenu** : Lien de validation avec token (expiration 24h)
- **Variables** : prenom, nom, token, lienValidation, dateExpiration

### 2. **PAIEMENT_REUSSI**
- **Quand** : Après un paiement réussi
- **Contenu** : Confirmation avec détails du paiement
- **Variables** : prenom, nom, montant, titreDeal, methodePaiement, datePaiement, descriptionDeal

### 3. **PAIEMENT_ECHOUE**
- **Quand** : Après un paiement échoué
- **Contenu** : Raison de l'échec et instructions
- **Variables** : prenom, nom, montant, titreDeal, raisonEchec, dateTentative, supportEmail

### 4. **RAPPEL_PAIEMENT**
- **Quand** : Avant la date d'échéance d'un paiement
- **Contenu** : Rappel avec lien de paiement
- **Variables** : prenom, nom, montant, titreDeal, dateEcheance, lienPaiement

### 5. **MISE_A_JOUR_COMPTE**
- **Quand** : Après modification des informations du compte
- **Contenu** : Détails des modifications
- **Variables** : prenom, nom, modifications, dateMiseAJour

### 6. **DESACTIVATION_COMPTE**
- **Quand** : Lors de la désactivation d'un compte
- **Contenu** : Raison et contact support
- **Variables** : prenom, nom, raisonDesactivation, dateDesactivation, supportEmail

### 7. **ACTIVATION_COMPTE**
- **Quand** : Lors de l'activation d'un compte
- **Contenu** : Confirmation avec lien de connexion
- **Variables** : prenom, nom, dateActivation, lienConnexion

### 8. **REINITIALISATION_MOT_DE_PASSE**
- **Quand** : Demande de réinitialisation de mot de passe
- **Contenu** : Lien de réinitialisation avec token (expiration 24h)
- **Variables** : prenom, nom, token, lienReinitialisation, dateExpiration

### 9. **DEAL_CREE** (Marchand)
- **Quand** : Création d'un nouveau deal
- **Contenu** : Détails du deal créé
- **Variables** : prenom, nom, titreDeal, prixDeal, nbParticipants, dateCreation, lienDeal

### 10. **DEAL_VALIDE** (Marchand)
- **Quand** : Validation d'un deal par l'admin
- **Contenu** : Confirmation de validation
- **Variables** : prenom, nom, titreDeal, dateValidation, lienDeal

### 11. **DEAL_ANNULE** (Marchand)
- **Quand** : Annulation d'un deal
- **Contenu** : Raison de l'annulation
- **Variables** : prenom, nom, titreDeal, raisonAnnulation, dateAnnulation, supportEmail

### 12. **NOUVEAU_PARTICIPANT_DEAL** (Marchand)
- **Quand** : Nouveau participant rejoint un deal
- **Contenu** : Informations du participant
- **Variables** : prenom, nom, titreDeal, prenomParticipant, nomParticipant, dateParticipation, lienDeal

### 13. **PAYOUT_DEAL** (Marchand)
- **Quand** : Paiement effectué au marchand
- **Contenu** : Détails du payout (montant total, frais, net)
- **Variables** : prenom, nom, titreDeal, montantTotal, fraisService, montantNet, datePayout

## 🔧 Configuration

### application.properties

```properties
# Configuration email
spring.mail.host=smtp.titan.email
spring.mail.port=587
spring.mail.username=deal@hanacalgary.ca
spring.mail.password=Mycontact@01
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Dépendances (bff-provider/pom.xml)

```xml
<!-- Spring Mail -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Thymeleaf pour les templates HTML -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

## 💻 Utilisation

### 1. Envoyer une notification de validation de compte

```java
@Component
@RequiredArgsConstructor
public class UtilisateurApiAdapter {
    
    private final NotificationService notificationService;
    
    public UtilisateurDTO creer(CreerUtilisateurDTO dto) {
        UtilisateurModele cree = utilisateurService.creer(modele);
        
        // Envoyer notification asynchrone
        notificationService.envoyerNotificationValidationCompte(cree);
        
        return mapper.modeleVersDto(cree);
    }
}
```

### 2. Envoyer une notification de paiement réussi

```java
public void traiterPaiement(UUID utilisateurUuid, BigDecimal montant, UUID dealUuid) {
    UtilisateurModele utilisateur = utilisateurService.lireParUuid(utilisateurUuid).get();
    DealModele deal = dealService.lireParUuid(dealUuid).get();
    
    notificationService.envoyerNotificationPaiementReussi(
        utilisateur, 
        montant, 
        deal, 
        "Carte bancaire"
    );
}
```

### 3. Valider un token de validation de compte

```java
@RestController
@RequestMapping("/api/validation")
public class ValidationResource {
    
    @GetMapping("/compte")
    public ResponseEntity<?> validerCompte(@RequestParam String token) {
        UUID utilisateurUuid = notificationService.validerTokenValidationCompte(token);
        // Activer le compte...
        return ResponseEntity.ok("Compte validé");
    }
}
```

## 🗄️ Base de données

### Table validation_token

```sql
CREATE TABLE validation_token (
    uuid UUID PRIMARY KEY,
    token VARCHAR(64) UNIQUE NOT NULL,
    utilisateur_uuid UUID NOT NULL,
    date_expiration TIMESTAMP NOT NULL,
    type_token VARCHAR(50) NOT NULL,
    utilise BOOLEAN DEFAULT false,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_uuid) REFERENCES utilisateur(uuid) ON DELETE CASCADE
);

CREATE INDEX idx_token ON validation_token(token);
CREATE INDEX idx_utilisateur_uuid ON validation_token(utilisateur_uuid);
```

## 🎨 Templates HTML

Les templates sont créés avec **Thymeleaf** et situés dans `resources/templates/notifications/`.

### Exemple de variables dans un template

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Validation de compte</title>
</head>
<body>
    <p>Bonjour <strong th:text="${prenom}">Prénom</strong> <strong th:text="${nom}">Nom</strong>,</p>
    <p>Cliquez sur le lien ci-dessous pour valider votre compte :</p>
    <a th:href="${lienValidation}">Valider mon compte</a>
    <p>Ce lien expire le <span th:text="${dateExpiration}">date</span></p>
</body>
</html>
```

## 🔐 Sécurité

### Tokens de validation

- **Génération** : UUID aléatoire sans tirets (32 caractères)
- **Expiration** : 24 heures
- **Stockage** : Base de données avec flag `utilise`
- **Utilisation unique** : Token supprimé après utilisation
- **Type** : VALIDATION_COMPTE ou REINITIALISATION_MOT_DE_PASSE

### Validation d'un token

```java
public UUID validerTokenValidationCompte(String token) {
    if (!notificationProvider.validerToken(token)) {
        throw new ValidationException("token.invalide.ou.expire");
    }
    
    UUID utilisateurUuid = notificationProvider.getUtilisateurUuidParToken(token);
    notificationProvider.supprimerToken(token);
    
    return utilisateurUuid;
}
```

## ⚡ Exécution asynchrone

Les emails sont envoyés de manière **asynchrone** grâce à l'annotation `@Async` :

```java
@Service
public class EmailService {
    
    @Async
    public void envoyerEmail(NotificationEmailModele notification) {
        // Envoi de l'email
    }
}
```

Configuration activée dans `AsyncConfiguration.java` :

```java
@Configuration
@EnableAsync
public class AsyncConfiguration {
}
```

## 📊 Flux de notification

```
1. Action utilisateur (inscription, paiement, etc.)
   ↓
2. ApiAdapter appelle NotificationService
   ↓
3. NotificationService crée NotificationEmailModele avec variables
   ↓
4. NotificationServiceImpl appelle NotificationProvider.envoyerEmailAsync()
   ↓
5. NotificationProviderAdapter sauvegarde le token (si présent)
   ↓
6. NotificationProviderAdapter appelle EmailService.envoyerEmail() (async)
   ↓
7. EmailService génère le HTML depuis le template Thymeleaf
   ↓
8. EmailService envoie l'email via JavaMailSender
   ↓
9. Utilisateur reçoit l'email
```

## 🧪 Tests

### Fichier HTTP

Utilisez le fichier `bff-http/notification.http` pour tester les endpoints :

```http
### Créer un utilisateur (envoie notification)
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@example.com",
  "motDePasse": "MotDePasse123!",
  "role": "CLIENT"
}

### Valider le compte
GET http://localhost:8080/api/validation/compte?token=VOTRE_TOKEN_ICI
```

## 📝 TODO

- [ ] Implémenter la méthode `reinitialiserMotDePasseSansToken()` dans `UtilisateurService`
- [ ] Ajouter la mise à jour du statut utilisateur à ACTIF lors de la validation
- [ ] Implémenter les notifications pour les deals (création, validation, etc.)
- [ ] Implémenter les notifications de paiement dans le système Square Payment
- [ ] Ajouter des tests unitaires pour NotificationServiceImpl
- [ ] Ajouter des tests d'intégration pour l'envoi d'emails
- [ ] Ajouter un système de retry en cas d'échec d'envoi
- [ ] Ajouter un dashboard admin pour voir l'historique des notifications
- [ ] Implémenter la suppression automatique des tokens expirés (tâche planifiée)

## 🎯 Bonnes pratiques

1. ✅ **Toujours envoyer les notifications de manière asynchrone** pour ne pas bloquer le thread principal
2. ✅ **Gérer les exceptions** lors de l'envoi pour ne pas bloquer les opérations métier
3. ✅ **Logger toutes les notifications** pour le suivi et le debug
4. ✅ **Personnaliser chaque email** avec le prénom et nom de l'utilisateur
5. ✅ **Utiliser des templates HTML professionnels** avec design responsive
6. ✅ **Expiration des tokens** : 24 heures maximum
7. ✅ **Token à usage unique** : suppression après utilisation
8. ✅ **Sécurité** : ne jamais exposer les tokens dans les logs

---

**Date de création** : 4 mars 2026  
**Auteur** : Équipe PayToGether  
**Version** : 1.0.0

