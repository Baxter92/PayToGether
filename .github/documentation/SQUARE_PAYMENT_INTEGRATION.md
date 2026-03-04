# Documentation Square Payment Integration - PayToGether

## 📋 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture](#architecture)
3. [Configuration](#configuration)
4. [Backend](#backend)
5. [Frontend](#frontend)
6. [Tests](#tests)
7. [Déploiement](#déploiement)
8. [Sécurité](#sécurité)

---

## 🎯 Vue d'ensemble

Cette intégration permet d'accepter des paiements via Square Payment API avec support pour :
- ✅ **Carte de crédit/débit**
- ✅ **Google Pay**
- ✅ **Apple Pay**
- ✅ **Cash App Pay**

### Flux de paiement

```
Frontend (React)
    ↓ Utilisateur saisit les infos
Square SDK
    ↓ Génère un token sécurisé
Backend API (POST /api/square-payments)
    ↓ Crée le paiement (statut EN_ATTENTE)
Event Dispatcher
    ↓ Publie PaymentInitiatedEvent
Event Handler
    ↓ Traite le paiement de manière asynchrone
Square API
    ↓ Confirme le paiement
Backend
    ↓ Met à jour le statut (CONFIRME/ECHOUE)
Event Dispatcher
    ↓ Publie PaymentSuccessfulEvent ou PaymentFailedEvent
Notification Handler
    ↓ Envoie email/SMS à l'utilisateur
```

---

## 🏗️ Architecture

### Architecture Hexagonale

L'implémentation suit strictement l'architecture hexagonale du projet :

```
┌─────────────────────────────────────────────────┐
│              BFF-API (Ports gauche)             │
│  SquarePaymentResource                          │
│  SquarePaymentApiAdapter                        │
│  DTOs (CreerPaiementSquareDTO, Response)        │
│  Handlers (SquarePaymentHandler)                │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│              BFF-CORE (Domaine métier)          │
│  SquarePaymentService / ServiceImpl             │
│  PaiementValidator (règles métier)              │
│  Providers (interfaces)                         │
│  Events (PaymentInitiatedEvent, etc.)           │
└─────────────────┬───────────────────────────────┘
                  │
        ┌─────────┴──────────┐
        │                    │
┌───────▼────────┐  ┌────────▼──────────┐
│  BFF-PROVIDER  │  │   BFF-WSCLIENT    │
│   (Droite)     │  │   (Externe)       │
│                │  │                   │
│ PaiementJpa    │  │ SquarePayment     │
│ Repository     │  │ ProviderAdapter   │
│ Mapper         │  │ (Square SDK)      │
└────────────────┘  └───────────────────┘
```

---

## ⚙️ Configuration

### Backend (application.properties)

```properties
# Configuration Square Payment
square.access-token=${SQUARE_ACCESS_TOKEN:votre-token-square}
square.environment=${SQUARE_ENVIRONMENT:SANDBOX}
square.location-id=${SQUARE_LOCATION_ID:votre-location-id}
square.application-id=${SQUARE_APPLICATION_ID:votre-application-id}
```

### Frontend (.env)

```env
VITE_SQUARE_APPLICATION_ID=sandbox-sq0idb-YOUR_APP_ID
VITE_SQUARE_LOCATION_ID=YOUR_LOCATION_ID
VITE_SQUARE_ENVIRONMENT=SANDBOX
```

### Obtenir les clés Square

1. Créer un compte sur https://developer.squareup.com/
2. Créer une application
3. Récupérer :
   - Application ID
   - Access Token
   - Location ID

---

## 🔧 Backend

### Modules créés/modifiés

#### 1. BFF-CORE

**Nouveaux fichiers** :
- `SquarePaymentService.java` (interface)
- `SquarePaymentServiceImpl.java` (implémentation)
- `SquarePaymentProvider.java` (interface externe)
- `PaiementValidator.java` (validations Square)

**Modifications** :
- `PaiementModele.java` : Ajout champs Square
- `MethodePaiement.java` : Ajout méthodes Square
- `StatutPaiement.java` : Ajout statuts (PROCESSING, REFUNDED, CANCELLED)

#### 2. BFF-PROVIDER

**Modifications** :
- `PaiementJpa.java` : Ajout colonnes Square
- `PaiementJpaMapper.java` : Mapping champs Square
- `PaiementProviderAdapter.java` : Méthodes Square
- `PaiementRepository.java` : Requêtes Square

#### 3. BFF-WSCLIENT

**Nouveaux fichiers** :
- `SquareClientConfig.java` : Configuration client
- `SquarePaymentProviderAdapter.java` : Implémentation API Square

**Dépendances** (pom.xml) :
```xml
<dependency>
    <groupId>com.squareup</groupId>
    <artifactId>square</artifactId>
    <version>46.0.0.20260122</version>
</dependency>
```

**Note** : Version vérifiée sur Maven Central - https://mvnrepository.com/artifact/com.squareup/square/46.0.0.20260122

#### 4. BFF-API

**Nouveaux fichiers** :
- `SquarePaymentResource.java` : Controller REST
- `SquarePaymentApiAdapter.java` : Adaptateur
- `CreerPaiementSquareDTO.java` : Request DTO
- `PaiementSquareResponseDTO.java` : Response DTO
- `SquarePaymentHandler.java` : Event handler

#### 5. BFF-EVENT

**Nouveaux fichiers** :
- `PaymentInitiatedEvent.java`
- `PaymentSuccessfulEvent.java`
- `PaymentFailedEvent.java`
- `PaymentNotificationEvent.java`

---

## 🎨 Frontend

### Fichiers créés

1. **Hook : `useSquarePayment.ts`**
   - Charge le SDK Square
   - Gère les mutations API
   - Types TypeScript

2. **Composant : `SquarePaymentForm.tsx`**
   - Formulaire de paiement
   - Support 4 méthodes
   - Gestion erreurs

### Utilisation

```tsx
import SquarePaymentForm from "@/pages/checkout/containers/SquarePaymentForm";

function CheckoutPage() {
  return (
    <SquarePaymentForm
      commandeUuid="550e8400-e29b-41d4-a716-446655440000"
      utilisateurUuid="550e8400-e29b-41d4-a716-446655440001"
      montant={99.99}
      onSuccess={(paymentId) => {
        console.log("Paiement réussi:", paymentId);
      }}
      onError={(error) => {
        console.error("Erreur paiement:", error);
      }}
    />
  );
}
```

---

## 🧪 Tests

### Tests Backend

Fichier : `modules/bff/bff-http/square-payment.http`

```http
### Créer un paiement Square avec carte
POST http://localhost:8080/api/square-payments
Content-Type: application/json

{
  "commandeUuid": "550e8400-e29b-41d4-a716-446655440000",
  "utilisateurUuid": "550e8400-e29b-41d4-a716-446655440001",
  "montant": 99.99,
  "squareToken": "cnon:card-nonce-ok",
  "methodePaiement": "SQUARE_CARD"
}
```

### Tests Frontend

```bash
cd modules/front
npm run dev
# Ouvrir http://localhost:5173/checkout
```

### Cartes de test Square (Sandbox)

| Numéro | Type | Résultat |
|--------|------|----------|
| 4111 1111 1111 1111 | Visa | Succès |
| 5105 1051 0510 5100 | Mastercard | Succès |
| 3782 822463 10005 | Amex | Succès |
| 4000 0000 0000 0002 | Visa | Décliné |

---

## 🚀 Déploiement

### Étapes de déploiement

1. **Configuration des variables d'environnement**
   ```bash
   export SQUARE_ACCESS_TOKEN=your-production-token
   export SQUARE_ENVIRONMENT=PRODUCTION
   export SQUARE_LOCATION_ID=your-location-id
   ```

2. **Migration de la base de données**
   ```bash
   # Liquibase appliquera automatiquement les migrations
   ./mvnw liquibase:update
   ```

3. **Build du backend**
   ```bash
   ./mvnw clean package -DskipTests
   ```

4. **Build du frontend**
   ```bash
   cd modules/front
   npm run build
   ```

5. **Démarrage**
   ```bash
   java -jar target/bff-configuration.jar
   ```

---

## 🔒 Sécurité

### Bonnes pratiques implémentées

1. ✅ **Tokens non stockés** : Les tokens Square sont utilisés une seule fois
2. ✅ **PCI DSS** : Aucune donnée de carte stockée côté serveur
3. ✅ **HTTPS obligatoire** : Tous les appels API en HTTPS
4. ✅ **Validation côté serveur** : Toutes les règles métier dans le Validator
5. ✅ **Events asynchrones** : Traitement en arrière-plan
6. ✅ **Logs sécurisés** : Aucune donnée sensible loguée

### Checklist de sécurité

- [ ] Configurer les clés de production Square
- [ ] Activer HTTPS sur tous les domaines
- [ ] Configurer les webhooks Square
- [ ] Tester les scénarios d'échec
- [ ] Configurer les alertes de paiement
- [ ] Documenter les procédures de remboursement

---

## 📊 Monitoring

### Événements à surveiller

1. **PaymentInitiatedEvent** : Paiement initié
2. **PaymentSuccessfulEvent** : Paiement réussi
3. **PaymentFailedEvent** : Paiement échoué
4. **HandlerFailedEvent** : Échec de traitement

### Métriques recommandées

- Taux de succès des paiements
- Temps moyen de traitement
- Nombre de remboursements
- Erreurs par type

---

## 🆘 Dépannage

### Problèmes courants

**Erreur : "Square Payment n'est pas initialisé"**
- Vérifier que le SDK est chargé
- Vérifier l'Application ID

**Erreur : "Token invalide"**
- Vérifier l'environnement (Sandbox vs Production)
- Régénérer les tokens

**Paiement bloqué en PROCESSING**
- Vérifier les webhooks Square
- Vérifier les logs du handler

---

## 📚 Ressources

- [Documentation Square](https://developer.squareup.com/docs)
- [SDK Square Web](https://developer.squareup.com/reference/sdks/web/payments)
- [Architecture PayToGether](../README_ARCHITECTURE.md)

---

**Auteur** : Équipe PayToGether  
**Date** : Mars 2026  
**Version** : 1.0.0

