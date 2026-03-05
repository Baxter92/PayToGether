# Instruction - Système de Notification par Email avec EventDispatcher

## 📋 Contexte

Le projet PayToGether utilise un **système d'événements (EventDispatcher)** pour gérer les notifications de manière asynchrone et découplée. Les propriétés de configuration email sont définies dans `application.properties`.

## 🏗️ Architecture Hexagonale

### Flux de notification (Architecture Hexagonale respectée)

```
Handler (bff-api) - Adaptateur d'entrée
    ↓
EmailNotificationService (bff-core) - Interface métier
    ↓
EmailNotificationServiceImpl (bff-core) - Implémentation métier (orchestration)
    ↓
EmailProvider (bff-core) - Interface du Port (sortie)
    ↓
EmailProviderAdapter (bff-provider) - Adaptateur du Port (implémentation technique)
    ↓
Infrastructure (JavaMailSender, Thymeleaf, SMTP)
```

### ⚠️ RÈGLES D'ARCHITECTURE IMPORTANTES

#### 1. **BFF-CORE** (Domaine métier - Aucune dépendance technique)

**Contient** :
- **Interfaces de Service** : `EmailNotificationService`
- **Implémentations de Service** : `EmailNotificationServiceImpl`
- **Interfaces de Provider (Ports)** : `EmailProvider`

**Règles** :
- ✅ Aucune dépendance à Spring Mail, JavaMailSender, Thymeleaf
- ✅ Services utilisent UNIQUEMENT les interfaces Provider
- ✅ Si pas de règle métier complexe : le ServiceImpl orchestre simplement les appels aux Providers
- ✅ Si règles métier : les Validators contiennent la logique de validation

#### 2. **BFF-PROVIDER** (Infrastructure technique - Adaptateurs)

**Contient** :
- **Adaptateurs de Provider** : `EmailProviderAdapter` implémente `EmailProvider`
- **Dépendances techniques** : JavaMailSender, Thymeleaf, JPA, etc.

**Règles** :
- ✅ Les ProviderAdapters implémentent les interfaces Provider du core
- ✅ C'est ICI qu'on utilise JavaMailSender, Thymeleaf, etc.
- ✅ Gestion des transactions (@Transactional)
- ✅ Interaction avec les repositories, services externes, etc.

#### 3. **BFF-API** (Adaptateurs d'entrée - Handlers)

**Contient** :
- **Handlers** : Traitent les événements
- **Utilisent** : Les Services du core (jamais les Providers directement)

**Règles** :
- ✅ Les Handlers utilisent UNIQUEMENT les Services du core
- ✅ JAMAIS d'appel direct aux Providers ou à l'infrastructure
- ✅ Séparation stricte entre orchestration (handlers) et métier (services)

### 📊 Flux complet avec EventDispatcher

```
1. Action métier (ex: UtilisateurService.creer())
   ↓
2. Dispatch d'événement : eventDispatcher.dispatchAsync(AccountValidationEvent)
   ↓
3. EventDispatcher enregistre en BDD (status: PENDING)
   ↓
4. EventConsumerService traite les événements PENDING
   ↓
5. AccountValidationHandler.handleAccountValidation()
   ├─ Sauvegarde du token en BDD
   └─ Appel : emailNotificationService.envoyerNotification()
       ↓
6. EmailNotificationServiceImpl (bff-core)
   └─ Orchestration : emailProvider.envoyerEmail()
       ↓
7. EmailProviderAdapter (bff-provider)
   ├─ Utilise JavaMailSender (infrastructure)
   ├─ Génère HTML avec Thymeleaf (infrastructure)
   └─ Envoie l'email via SMTP
       ↓
8. Événement marqué PROCESSED ou FAILED
```

## 🛠️ Implémentation

### 1. Créer l'interface Provider (bff-core/provider)

```java
package com.ulr.paytogether.core.provider;

import java.util.Map;

/**
 * Interface Provider pour l'envoi d'emails (Port de sortie)
 */
public interface EmailProvider {
    void envoyerEmail(String destinataire, String sujet, String templateName, Map<String, Object> variables);
}
```

### 2. Créer le Service métier (bff-core/domaine)

**Interface** :
```java
package com.ulr.paytogether.core.domaine.service;

import java.util.Map;

public interface EmailNotificationService {
    void envoyerNotification(String destinataire, String sujet, String templateName, Map<String, Object> variables);
}
```

**Implémentation** :
```java
package com.ulr.paytogether.core.domaine.impl;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {
    
    private final EmailProvider emailProvider;  // Interface du Port
    
    @Override
    public void envoyerNotification(String destinataire, String sujet, String templateName, Map<String, Object> variables) {
        log.info("Service - Envoi de notification email à: {}", destinataire);
        
        // Pas de règle métier complexe ici, juste orchestration
        emailProvider.envoyerEmail(destinataire, sujet, templateName, variables);
    }
}
```

### 3. Créer l'adaptateur Provider (bff-provider/adapter)

```java
package com.ulr.paytogether.provider.adapter;

@Component  // Pas @Service ! C'est un adaptateur
@RequiredArgsConstructor
@Slf4j
public class EmailProviderAdapter implements EmailProvider {
    
    private final JavaMailSender mailSender;  // Infrastructure
    private final TemplateEngine templateEngine;  // Infrastructure
    
    @Value("${spring.mail.username}")
    private String expediteur;
    
    @Override
    public void envoyerEmail(String destinataire, String sujet, String templateName, Map<String, Object> variables) {
        log.info("Provider - Envoi d'email à: {}", destinataire);
        
        // 1. Utilise l'infrastructure technique (JavaMailSender)
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(expediteur);
        helper.setTo(destinataire);
        helper.setSubject(sujet);
        
        // 2. Génère le HTML avec Thymeleaf (infrastructure)
        Context context = new Context();
        if (variables != null) {
            variables.forEach(context::setVariable);
        }
        String html = templateEngine.process("notifications/" + templateName, context);
        helper.setText(html, true);
        
        // 3. Envoie via SMTP
        mailSender.send(message);
    }
}
```

### 4. Utiliser dans le Handler (bff-api/handler)

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountValidationHandler implements ConsumerHandler {
    
    private final EmailNotificationService emailNotificationService;  // Service métier
    private final ValidationTokenRepository tokenRepository;
    
    @FunctionalHandler(eventType = AccountValidationEvent.class, maxAttempts = 3)
    public void handleAccountValidation(AccountValidationEvent event) {
        // 1. Sauvegarder le token en BDD
        ValidationTokenJpa token = ValidationTokenJpa.builder()
                .token(event.getToken())
                .utilisateurUuid(event.getUtilisateurUuid())
                .dateExpiration(event.getDateExpiration())
                .typeToken("VALIDATION_COMPTE")
                .build();
        tokenRepository.save(token);
        
        // 2. Préparer les variables
        Map<String, Object> variables = Map.of(
            "prenom", event.getPrenom(),
            "nom", event.getNom(),
            "token", event.getToken(),
            "lienValidation", "https://dev.dealtogether.ca/validation?token=" + event.getToken()
        );
        
        // 3. Appeler le service métier (PAS le provider directement !)
        emailNotificationService.envoyerNotification(
            event.getEmail(),
            "Validation de votre compte PayToGether",
            "notification-account-validation",
            variables
        );
    }
}
```

## 📦 Structure des modules

### bff-event (Événements métier)
- **model/** : Événements de notification (héritent de `DomainEvent`)
  - `AccountValidationEvent.java`
  - `PaymentSuccessfulNotificationEvent.java`
  - `PaymentFailedNotificationEvent.java`
  - `PaymentReminderEvent.java`
  - `AccountUpdateNotificationEvent.java`
  - `AccountDeactivationEvent.java`
  - `AccountActivationEvent.java`
  - `PasswordResetEvent.java`
  - `DealCreatedEvent.java`
  - `DealValidatedEvent.java`
  - `DealCancelledEvent.java`
  - `NewParticipantEvent.java`
  - `PayoutCompletedEvent.java`

### bff-event-dispatcher (Traitement des événements)
- **consumer/** : `EventConsumerService` (traite les événements PENDING)
- **dispatcher/** : `EventDispatcherImpl` (enregistre les événements)

### bff-api (Handlers de notification)
- **handler/** : Handlers spécifiques annotés avec `@FunctionalHandler`
  - `AccountValidationHandler.java`
  - `PaymentNotificationHandler.java`
  - `DealNotificationHandler.java`
  - etc.

### bff-provider (Infrastructure email)
- **service/** : `EmailService` (envoi d'emails avec Thymeleaf)

### bff-configuration (Templates HTML)
- **resources/templates/notifications/** : Templates HTML personnalisés

## 🎯 Notifications à implémenter

### 1. **Validation de compte** 🔐
- **Événement** : `AccountValidationEvent`
- **Quand** : 
  - Lors de l'inscription depuis `AuthResource.register()`
  - Lors de la création par l'admin depuis `UtilisateurResource.creer()`
- **Contenu** :
  - Token de validation (expiration 24h)
  - Lien de confirmation → `PATHS.SUCCESS_SUBSCRIPTION()`
  - Variables : `prenom`, `nom`, `token`, `lienValidation`, `dateExpiration`
- **Template** : `notification-account-validation.html`

### 2. **Paiement réussi** ✅
- **Événement** : `PaymentSuccessfulNotificationEvent`
- **Quand** : Après un paiement réussi
- **Contenu** :
  - Montant, méthode de paiement
  - Détails du deal (titre, description)
  - Message de remerciement
  - Variables : `prenom`, `nom`, `montant`, `methodePaiement`, `titreDeal`, `descriptionDeal`
- **Template** : `notification-payment-successful.html`

### 3. **Paiement échoué** ❌
- **Événement** : `PaymentFailedNotificationEvent`
- **Quand** : Après un paiement échoué
- **Contenu** :
  - Montant, méthode de paiement
  - Raison de l'échec
  - Instructions pour résoudre le problème
  - Contact support client
  - Variables : `prenom`, `nom`, `montant`, `methodePaiement`, `titreDeal`, `raisonEchec`, `supportEmail`
- **Template** : `notification-payment-failed.html`

### 4. **Rappel de paiement** ⏰
- **Événement** : `PaymentReminderEvent`
- **Quand** : Avant la date d'échéance d'un paiement
- **Contenu** :
  - Montant, date d'échéance
  - Instructions pour effectuer le paiement
  - Lien vers la page de paiement
  - Variables : `prenom`, `nom`, `montant`, `dateEcheance`, `lienPaiement`
- **Template** : `notification-payment-reminder.html`

### 5. **Mise à jour de compte** 📝
- **Événement** : `AccountUpdateNotificationEvent`
- **Quand** : Après modification du profil
- **Contenu** :
  - Détails des modifications (nom, prénom, email, etc.)
  - Message de confirmation
  - Variables : `prenom`, `nom`, `modifications`, `dateMiseAJour`
- **Template** : `notification-account-update.html`

### 6. **Désactivation de compte** 🔒
- **Événement** : `AccountDeactivationEvent`
- **Quand** : Après désactivation du compte
- **Contenu** :
  - Raison de la désactivation
  - Instructions pour réactiver
  - Contact support client
  - Variables : `prenom`, `nom`, `raisonDesactivation`, `supportEmail`
- **Template** : `notification-account-deactivation.html`

### 7. **Activation de compte par API** ✅
- **Événement** : `AccountActivationEvent`
- **Quand** : Après activation du compte (par admin ou validation)
- **Contenu** :
  - Confirmation d'activation
  - Lien vers la page de connexion
  - Variables : `prenom`, `nom`, `dateActivation`, `lienConnexion`
- **Template** : `notification-account-activation.html`

### 8. **Réinitialisation de mot de passe** 🔑
- **Événement** : `PasswordResetEvent`
- **Quand** : Demande de réinitialisation de mot de passe
- **Contenu** :
  - Lien de réinitialisation (token 24h)
  - Instructions claires
  - Variables : `prenom`, `nom`, `token`, `lienReinitialisation`, `dateExpiration`
- **Template** : `notification-password-reset.html`

### 9. **Statut du deal pour le marchand** 🎯
- **Événements** : 
  - `DealCreatedEvent` : Nouveau deal créé
  - `DealValidatedEvent` : Deal validé par l'admin
  - `DealCancelledEvent` : Deal annulé
- **Contenu** :
  - Détails du deal (titre, montant, date, statut)
  - Instructions selon le statut
  - Variables : `prenom`, `nom`, `titreDeal`, `montant`, `statut`, `dateAction`
- **Templates** : 
  - `notification-deal-created.html`
  - `notification-deal-validated.html`
  - `notification-deal-cancelled.html`

### 10. **Nouveau participant à un deal** 👥
- **Événement** : `NewParticipantEvent`
- **Quand** : Un nouveau participant rejoint un deal
- **Contenu** :
  - Informations du participant (prénom, nom)
  - Détails du deal
  - Instructions pour gérer le deal
  - Variables : `prenomMarchand`, `nomMarchand`, `prenomParticipant`, `nomParticipant`, `titreDeal`
- **Template** : `notification-new-participant.html`

### 11. **Payout d'un deal au marchand** 💰
- **Événement** : `PayoutCompletedEvent`
- **Quand** : Payout effectué au marchand
- **Contenu** :
  - Montant total payé
  - Frais de service
  - Montant net reçu
  - Date de paiement
  - Variables : `prenomMarchand`, `nomMarchand`, `titreDeal`, `montantTotal`, `fraisService`, `montantNet`, `datePaiement`
- **Template** : `notification-payout-completed.html`

## 🛠️ Implémentation

### 1. Créer les événements (bff-event/model)

```java
@Getter
@NoArgsConstructor
public class AccountValidationEvent extends DomainEvent {
    private UUID utilisateurUuid;
    private String email;
    private String prenom;
    private String nom;
    private String token;
    private LocalDateTime dateExpiration;
    
    @JsonCreator
    public AccountValidationEvent(
            @JsonProperty("utilisateurUuid") UUID utilisateurUuid,
            @JsonProperty("email") String email,
            @JsonProperty("prenom") String prenom,
            @JsonProperty("nom") String nom,
            @JsonProperty("token") String token,
            @JsonProperty("dateExpiration") LocalDateTime dateExpiration) {
        super("UtilisateurService");
        this.utilisateurUuid = utilisateurUuid;
        this.email = email;
        this.prenom = prenom;
        this.nom = nom;
        this.token = token;
        this.dateExpiration = dateExpiration;
    }
    
    @Override
    public String toJson() {
        // Sérialisation JSON
    }
}
```

### 2. Dispatcher l'événement depuis le service métier

```java
@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {
    
    private final EventDispatcher eventDispatcher;
    private final UtilisateurProvider utilisateurProvider;
    
    @Override
    public UtilisateurModele creer(UtilisateurModele utilisateur) {
        // 1. Validation métier
        validator.valider(utilisateur);
        
        // 2. Créer l'utilisateur
        UtilisateurModele cree = utilisateurProvider.sauvegarder(utilisateur);
        
        // 3. Générer token de validation
        String token = genererToken();
        LocalDateTime expiration = LocalDateTime.now().plusHours(24);
        
        // 4. Dispatcher l'événement de validation
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

### 3. Créer le handler (bff-api/handler)

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
        description = "Envoie un email de validation de compte"
    )
    public void handleAccountValidation(AccountValidationEvent event) {
        log.info("Traitement événement validation compte: {}", event.getUtilisateurUuid());
        
        try {
            // 1. Sauvegarder le token en BDD
            ValidationTokenJpa tokenJpa = ValidationTokenJpa.builder()
                .token(event.getToken())
                .utilisateurUuid(event.getUtilisateurUuid())
                .dateExpiration(event.getDateExpiration())
                .typeToken("VALIDATION_COMPTE")
                .build();
            tokenRepository.save(tokenJpa);
            
            // 2. Préparer les variables du template
            Map<String, Object> variables = Map.of(
                "prenom", event.getPrenom(),
                "nom", event.getNom(),
                "token", event.getToken(),
                "lienValidation", construireLienValidation(event.getToken()),
                "dateExpiration", formatterDate(event.getDateExpiration())
            );
            
            // 3. Envoyer l'email
            emailService.envoyerEmail(
                event.getEmail(),
                "Validation de votre compte PayToGether",
                "notification-account-validation",
                variables
            );
            
            log.info("Email de validation envoyé à: {}", event.getEmail());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de validation: {}", e.getMessage(), e);
            throw e; // Propagation pour retry automatique
        }
    }
    
    private String construireLienValidation(String token) {
        return PATHS.SUCCESS_SUBSCRIPTION() + "?token=" + token;
    }
}
```

### 4. EmailService (bff-provider/service)

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String expediteur;
    
    public void envoyerEmail(String destinataire, String sujet, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(expediteur);
            helper.setTo(destinataire);
            helper.setSubject(sujet);
            
            // Génération du contenu HTML depuis le template
            Context context = new Context();
            variables.forEach(context::setVariable);
            String contenuHtml = templateEngine.process("notifications/" + templateName, context);
            
            helper.setText(contenuHtml, true);
            
            mailSender.send(message);
            log.info("Email envoyé à: {}", destinataire);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi d'email: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur envoi email", e);
        }
    }
}
```

### 5. Templates HTML (resources/templates/notifications/)

Créer les templates HTML avec Thymeleaf :
- `notification-account-validation.html`
- `notification-payment-successful.html`
- `notification-payment-failed.html`
- `notification-payment-reminder.html`
- `notification-account-update.html`
- `notification-account-deactivation.html`
- `notification-account-activation.html`
- `notification-password-reset.html`
- `notification-deal-created.html`
- `notification-deal-validated.html`
- `notification-deal-cancelled.html`
- `notification-new-participant.html`
- `notification-payout-completed.html`

Chaque template doit :
- Être personnalisé avec `prenom` et `nom`
- Utiliser des variables dynamiques Thymeleaf (`th:text="${variable}"`)
- Avoir un design professionnel et responsive
- Inclure des instructions claires pour l'utilisateur

## ✅ Checklist d'implémentation

### Pour chaque type de notification :

1. **Créer l'événement** (bff-event/model)
   - [ ] Hériter de `DomainEvent`
   - [ ] Ajouter les propriétés nécessaires
   - [ ] Annoter avec `@JsonCreator` et `@JsonProperty`
   - [ ] Implémenter `toJson()`

2. **Dispatcher l'événement** (Services métier)
   - [ ] Injecter `EventDispatcher`
   - [ ] Créer l'événement après l'action métier
   - [ ] Utiliser `dispatchAsync(event)` pour envoi asynchrone

3. **Créer le handler** (bff-api/handler)
   - [ ] Implémenter `ConsumerHandler`
   - [ ] Annoter la méthode avec `@FunctionalHandler`
   - [ ] Gérer les erreurs pour permettre le retry
   - [ ] Logger les actions

4. **Créer le template HTML** (bff-configuration/resources/templates/notifications/)
   - [ ] Design professionnel avec gradient
   - [ ] Variables dynamiques Thymeleaf
   - [ ] Responsive design
   - [ ] Instructions claires

5. **Tester** (bff-http)
   - [ ] Créer des tests dans fichier `.http`
   - [ ] Vérifier l'envoi d'email
   - [ ] Vérifier les retry en cas d'échec
   - [ ] Vérifier la traçabilité en BDD

## 🔒 Sécurité

- ✅ **Tokens** : UUID unique, expiration 24h, usage unique
- ✅ **HTTPS** : Tous les liens doivent utiliser HTTPS
- ✅ **Pas de données sensibles** : Ne jamais inclure de mot de passe dans les emails
- ✅ **Validation** : Toujours valider les tokens côté serveur

## 📊 Traçabilité

Tous les événements sont enregistrés dans la table `event_record` avec :
- `event_id` : UUID unique
- `event_type` : Type d'événement
- `payload` : JSON de l'événement
- `status` : PENDING, PROCESSING, PROCESSED, FAILED
- `attempts` : Nombre de tentatives
- `processed_at` : Date de traitement

## 🎯 Résultat attendu

À la fin de l'implémentation, chaque action utilisateur déclenchera automatiquement l'envoi d'un email personnalisé :
- ✅ Inscription → Email de validation
- ✅ Paiement → Confirmation ou notification d'échec
- ✅ Mise à jour profil → Confirmation
- ✅ Création deal → Notification au marchand
- ✅ Nouveau participant → Notification au marchand
- ✅ Payout → Notification au marchand

**Tous les emails sont envoyés de manière asynchrone, avec retry automatique en cas d'échec, et traçabilité complète en base de données.**

