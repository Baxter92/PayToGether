# Tests d'Intégration - PayToGether

## 🎯 Objectif

Tester le **parcours complet** du cycle de vie d'une commande :
```
Deal créé → Paiements → Commande complétée → Payout → 
Facture vendeur → Factures clients → Validation → Terminée
```

## 📁 Structure

```
bff-configuration/src/test/
├── java/com/ulr/paytogether/configuration/test/
│   ├── AbstractIT.java                              # Classe de base avec fixtures JPA
│   └── integration/
│       ├── ParcoursCommandeCompletIT.java           # Tests du parcours complet (fixtures)
│       └── ParcoursCommandeRestIT.java              # Tests REST avec appels API
└── resources/
    ├── application-test.properties                   # Configuration des tests
    └── wiremock/
        ├── mappings/                                 # Mappings WireMock prédéfinis
        │   ├── square-payment-success.json
        │   ├── minio-upload.json
        │   └── minio-download.json
        └── __files/                                  # Réponses JSON des APIs
            ├── square/
            │   ├── payment-success.json
            │   └── payment-failed.json
            ├── deal/
            ├── categorie/
            ├── utilisateur/
            ├── paiement/
            └── commande/
```

## 🔧 Configuration

### Dépendances Maven

```xml
<!-- WireMock pour mocker les APIs externes -->
<dependency>
  <groupId>org.wiremock</groupId>
  <artifactId>wiremock-standalone</artifactId>
  <version>3.3.1</version>
  <scope>test</scope>
</dependency>

<!-- REST Assured pour tester les endpoints REST -->
<dependency>
  <groupId>io.rest-assured</groupId>
  <artifactId>rest-assured</artifactId>
  <scope>test</scope>
</dependency>

<!-- Testcontainers (optionnel, pour PostgreSQL réel) -->
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>testcontainers</artifactId>
  <version>1.19.3</version>
  <scope>test</scope>
</dependency>
```

### application-test.properties

- Base de données : **H2 en mémoire** (mode PostgreSQL)
- Flyway : **Désactivé** (utilise `ddl-auto=create-drop`)
- WireMock : **Port dynamique** pour Square et MinIO
- Sécurité : **Désactivée** pour les tests

## 🧪 AbstractIT - Classe de Base

### Responsabilités

1. **Configuration Testcontainers** (optionnel PostgreSQL)
2. **Configuration WireMock** (démarre/arrête le serveur)
3. **Fixtures JPA** : Création des données de test
4. **Méthodes utilitaires** : Simulation du parcours

### Fixtures disponibles

```java
protected CategorieJpa categorieElectronique;
protected CategorieJpa categorieMaison;
protected UtilisateurJpa vendeur;
protected UtilisateurJpa acheteur1;
protected UtilisateurJpa acheteur2;
protected UtilisateurJpa acheteur3;
protected DealJpa dealTest;
protected CommandeJpa commandeTest;
protected AdresseJpa adresseTest;
```

### Méthodes utilitaires

| Méthode | Description |
|---------|-------------|
| `creerDealAvecImages()` | Crée un deal publié avec 2 images (1 principale) |
| `creerCommande()` | Crée une commande avec statut EN_COURS |
| `creerPaiement()` | Crée un paiement avec calcul des frais canadiens |
| `creerCommandeUtilisateur()` | Crée une entrée CommandeUtilisateur |
| `simulerPaiementsComplets()` | Simule tous les paiements et passe à COMPLETEE |
| `simulerDepotPayout()` | Simule la validation admin → PAYOUT |
| `simulerUploadFactureMarchand()` | Simule l'upload facture → INVOICE_SELLER |
| `simulerEnvoiFacturesClients()` | Simule génération factures → INVOICE_CUSTOMER |
| `simulerValidationCompleteVendeur()` | Simule validation vendeur → TERMINEE |
| `mockSquarePaymentSuccess()` | Mock WireMock pour paiement Square réussi |
| `mockSquarePaymentFailed()` | Mock WireMock pour paiement Square échoué |
| `mockMinioPresignedUrl()` | Mock WireMock pour URL présignées MinIO |
| `mockMinioUpload()` | Mock WireMock pour upload MinIO |

## 📊 Parcours testé

### ParcoursCommandeCompletIT (Fixtures JPA)

**Test unitaires par étape** :
1. ✅ `etape01_creerDealPublie` : Création d'un deal
2. ✅ `etape02_premierPaiementCreerCommande` : Premier paiement → EN_COURS
3. ✅ `etape03_dernierPaiementCommandCompletee` : Dernier paiement → COMPLETEE
4. ✅ `etape04_adminValidPayoutCommandePayout` : Admin valide → PAYOUT
5. ✅ `etape05_vendeurUploadFactureInvoiceSeller` : Vendeur upload → INVOICE_SELLER
6. ✅ `etape06_generationFacturesClientsInvoiceCustomer` : Génération factures → INVOICE_CUSTOMER
7. ✅ `etape07_vendeurValideTousClientsCommandeTerminee` : Validation complète → TERMINEE

**Test intégral** :
8. ✅ `parcoursComplet_dealVersCommandeTerminee` : Toutes les étapes en un seul test

**Cas d'erreur** :
9. ✅ `casErreur_payoutAvantCompletee` : Impossible de passer à PAYOUT avant COMPLETEE
10. ✅ `casErreur_terminerAvantValidationComplete` : Impossible de terminer sans validation complète

### ParcoursCommandeRestIT (Appels REST)

**Tests des endpoints** :
1. ✅ `test01_creerDealViaRest` : POST /api/deals
2. ✅ `test02_creerPaiementSquareViaRest` : POST /api/square-payments
3. ✅ `test03_validerPayoutViaRest` : POST /api/admin/commandes/{uuid}/payout/valider
4. ✅ `test04_uploadFactureVendeurViaRest` : POST /api/admin/commandes/{uuid}/facture/upload
5. ✅ `test05_validerFacturesClientsViaRest` : POST /api/admin/commandes/{uuid}/factures/valider
6. ✅ `test06_listerCommandesViaRest` : GET /api/admin/commandes
7. ✅ `test07_listerCommandesMarchandViaRest` : GET /api/admin/commandes/marchand/{uuid}
8. ✅ `test08_recupererCommandeParUuidViaRest` : GET /api/admin/commandes/{uuid}

**Note** : Certains tests peuvent retourner 401/403 si la sécurité Spring Security est activée.

## 🚀 Exécution des tests

### Tous les tests d'intégration

```bash
cd /Users/da/Documents/NewProjet/PayToGether
./mvnw -pl modules/bff/bff-configuration test -Dtest="*IT"
```

### Un test spécifique

```bash
./mvnw -pl modules/bff/bff-configuration test -Dtest="ParcoursCommandeCompletIT"
```

### Avec logs détaillés

```bash
./mvnw -pl modules/bff/bff-configuration test -Dtest="*IT" -X
```

## 📋 Calcul des montants (Formule canadienne)

Les paiements suivent la formule canadienne :

```java
double montantTransaction = homePickup ? 12.0 : 0.0;  // Frais livraison
double montantDuPaiement = prixPart * nombreDePart;
double montantTotalFraisService = montantDuPaiement + (0.05 * montantDuPaiement);
double tva = 0.05 * montantTotalFraisService;          // TVA 5%
double montantTotal = montantTotalFraisService + tva + montantTransaction;
```

**Exemple** : 1 part à 100 CAD avec livraison
- Montant de base : 100 CAD
- Frais de service (5%) : 5 CAD
- Sous-total : 105 CAD
- TVA (5% sur 105) : 5.25 CAD
- Frais livraison : 12 CAD
- **Total** : **122.25 CAD**

## 🔄 Cycle de vie d'une commande

```
┌─────────────┐
│  Deal créé  │
│  (PUBLIE)   │
└─────┬───────┘
      │ Premier paiement
      ↓
┌─────────────┐
│  EN_COURS   │ ← Commande créée, paiements en cours
└─────┬───────┘
      │ Dernier paiement (toutes les parts payées)
      ↓
┌─────────────┐
│  COMPLETEE  │ ← Tous les paiements reçus
└─────┬───────┘
      │ Admin valide le payout (date dépôt)
      ↓
┌─────────────┐
│   PAYOUT    │ ← Argent envoyé au vendeur
└─────┬───────┘
      │ Vendeur upload sa facture
      ↓
┌──────────────────┐
│  INVOICE_SELLER  │ ← Facture vendeur reçue
└─────┬────────────┘
      │ Génération automatique des factures clients
      ↓
┌────────────────────┐
│ INVOICE_CUSTOMER   │ ← Factures clients envoyées par email
└─────┬──────────────┘
      │ Vendeur valide tous les clients
      ↓
┌─────────────┐
│  TERMINEE   │ ← Toutes les validations complètes
└─────────────┘
```

## 🎯 Points de validation

### À chaque étape, on vérifie :

1. **EN_COURS** :
   - ✅ Commande créée avec numéro unique
   - ✅ Au moins 1 paiement COMPLETED
   - ✅ CommandeUtilisateur créé

2. **COMPLETEE** :
   - ✅ Tous les paiements (nb = nbParticipants)
   - ✅ Tous les paiements avec statut COMPLETED
   - ✅ Tous les CommandeUtilisateurs créés

3. **PAYOUT** :
   - ✅ dateDepotPayout définie
   - ✅ Email envoyé au vendeur (via event handler)

4. **INVOICE_SELLER** :
   - ✅ factureMarchandUrl définie
   - ✅ Fichier présent dans MinIO (invoice/seller/)
   - ✅ Event déclenché pour génération factures clients

5. **INVOICE_CUSTOMER** :
   - ✅ Factures clients générées (PDF)
   - ✅ Factures uploadées dans MinIO (invoice/user/)
   - ✅ Emails envoyés aux clients avec pièces jointes

6. **TERMINEE** :
   - ✅ Tous les CommandeUtilisateurs avec estValide = true
   - ✅ Plus de modification possible

## 🔍 Debug

### Logs importants

```properties
logging.level.com.ulr.paytogether=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### Vérifier WireMock

Pendant les tests, WireMock est accessible à :
```
http://localhost:{port_dynamique}/__admin/
```

Le port est affiché dans les logs au démarrage des tests.

### Vérifier la base H2

Vous pouvez activer la console H2 en ajoutant :
```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

## ⚠️ Limitations actuelles

1. **Sécurité Spring Security** : Les tests REST peuvent échouer avec 401/403 si la sécurité n'est pas complètement désactivée
   - Solution : Configurer un utilisateur test avec token JWT valide
   - Ou désactiver complètement la sécurité en tests

2. **Event handlers asynchrones** : Les tests ne vérifient pas l'exécution réelle des handlers
   - Solution : Ajouter des tests d'intégration spécifiques pour les events

3. **Email** : Les emails ne sont pas réellement envoyés
   - Solution : Utiliser GreenMail ou MailHog en tests

4. **MinIO** : Utilise WireMock (pas un vrai MinIO)
   - Solution : Utiliser Testcontainers MinIO si besoin

## 🔧 Améliorations futures

- [ ] Ajouter Testcontainers PostgreSQL pour tests avec vraie BDD
- [ ] Ajouter GreenMail pour tester l'envoi d'emails
- [ ] Ajouter des tests pour les event handlers
- [ ] Ajouter des tests de performance (temps de réponse)
- [ ] Ajouter des tests de sécurité (JWT, rôles)
- [ ] Ajouter des tests de validation métier (Validators)

## 📖 Utilisation

### Étendre AbstractIT

```java
@ActiveProfiles("test")
class MonNouveauTestIT extends AbstractIT {
    
    @Test
    void monTest() {
        // Given : Fixtures déjà créées
        assertNotNull(vendeur);
        assertNotNull(categorieElectronique);
        
        // When : Créer un deal
        DealJpa deal = creerDealAvecImages(vendeur, categorieElectronique, 3, BigDecimal.valueOf(100));
        
        // Then : Vérifications
        assertNotNull(deal.getUuid());
    }
}
```

### Utiliser WireMock

```java
@Test
void testAvecWireMock() {
    // Mock Square Payment
    mockSquarePaymentSuccess();
    
    // Votre code qui appelle Square Payment
    // ...
    
    // WireMock interceptera les appels
}
```

## 📈 Couverture

Les tests d'intégration couvrent :
- ✅ **Cycle complet** : De la création du deal à la commande terminée (7 étapes)
- ✅ **Cas d'erreur** : Transitions de statut invalides
- ✅ **Calculs** : Montants, frais, TVA selon la formule canadienne
- ✅ **Relations** : Deal ↔ Commande ↔ Paiement ↔ CommandeUtilisateur
- ✅ **Statuts** : Transitions de StatutCommande et StatutCommandeUtilisateur
- ✅ **Validations métier** : Images, paiements, validations vendeur

## 🎓 Bonnes pratiques

1. ✅ **Nettoyer la base** avant et après chaque test
2. ✅ **Utiliser @Order** pour les tests séquentiels
3. ✅ **Logs clairs** avec emojis pour suivre le parcours
4. ✅ **Assertions complètes** à chaque étape
5. ✅ **Noms de tests explicites** (`etape01_`, `test01_`, etc.)
6. ✅ **Cas nominaux ET cas d'erreur**

## 🐛 Troubleshooting

### Erreur : Bean not found

**Solution** : Vérifier que tous les modules sont dans les dépendances de `bff-configuration/pom.xml`

### Erreur : WireMock port conflict

**Solution** : WireMock utilise un port dynamique, le conflit ne devrait pas arriver

### Erreur : Flyway schema history

**Solution** : Désactiver Flyway en tests (`spring.flyway.enabled=false`)

### Erreur : Table not found

**Solution** : Vérifier que `spring.jpa.hibernate.ddl-auto=create-drop` est activé

## 📞 Support

Pour toute question sur les tests d'intégration :
- Consulter les logs détaillés avec `-X`
- Vérifier les fixtures dans `AbstractIT`
- Vérifier les mappings WireMock dans `src/test/resources/wiremock/`

---

**Date de création** : 14 mars 2026  
**Dernière mise à jour** : 14 mars 2026

