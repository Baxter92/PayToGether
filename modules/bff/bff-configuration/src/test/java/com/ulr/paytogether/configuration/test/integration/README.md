# Tests d'Intégration - Parcours Commande

## 📋 Vue d'ensemble

Ce module contient les tests d'intégration REST complets pour valider le parcours d'une commande du début à la fin :
1. Création d'un deal
2. Paiements des acheteurs (via Square)
3. Validation du payout
4. Upload de la facture vendeur
5. Validation des factures clients
6. Commande terminée

## 🔧 Configuration

### Sécurité en Tests

La sécurité est **désactivée** en tests via `TestSecurityConfig` :
- ✅ Pas besoin de token JWT
- ✅ Tous les endpoints sont accessibles
- ✅ Simplifie les tests d'intégration

```java
@TestConfiguration
public class TestSecurityConfig {
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                .build();
    }
}
```

### WireMock

WireMock est utilisé pour mocker :
- ✅ MinIO (upload d'images, génération d'URL présignées)
- ✅ Square Payment API
- ✅ Services externes (JWT, email, etc.)

Les stubs WireMock sont configurés dans `AbstractIT` :
```java
protected void mockMinioPresignedUrl() { ... }
protected void mockMinioUpload() { ... }
protected void mockSquarePaymentSuccess() { ... }
```

### Base de données

- **H2 en mémoire** par défaut (rapide, isolation complète)
- **PostgreSQL Testcontainers** disponible (décommenter dans `AbstractIT`)

### RestAssured

Configuration dans `AbstractIT.setupRestAssured()` :
```java
RestAssured.port = port;                 // Port dynamique du serveur de test
RestAssured.basePath = "/api";           // Base path pour tous les appels
```

## 🧪 Structure des Tests

### AbstractIT

Classe de base contenant :
- Configuration WireMock
- Fixtures JPA (catégories, utilisateurs, deals, etc.)
- Méthodes utilitaires (création de deal, commande, paiement)
- Nettoyage de la base de données après chaque test

### ParcoursCommandeRestIT

Tests d'intégration REST validant le parcours d'une **commande** :

#### Tests Unitaires
1. **test01_creerDealViaRest** : Création d'un deal via POST /api/deals
2. **test02_creerPaiementSquareViaRest** : Paiement via POST /api/square-payments
3. **test03_validerPayoutViaRest** : Validation du payout
4. **test04_uploadFactureVendeurViaRest** : Upload de la facture
5. **test05_validerFacturesClientsViaRest** : Validation des factures clients
6. **test06_listerCommandesViaRest** : Listage des commandes
7. **test07_listerCommandesMarchandViaRest** : Commandes d'un marchand
8. **test08_recupererCommandeParUuidViaRest** : Récupération d'une commande

#### Test de Parcours Complet
9. **parcoursRestComplet_dealVersCommandeTerminee** : Parcours E2E complet

```
Deal créé → 3 Paiements → Commande COMPLETEE → Payout → Facture → TERMINEE
```

### ParcoursUtilisateurRestIT

Tests d'intégration REST validant le parcours complet d'un **utilisateur** :

#### Tests Unitaires
1. **test01_creerUtilisateur** : Inscription d'un nouvel utilisateur (POST /api/auth/register)
2. **test02_activerCompteUtilisateur** : Activation du compte (INACTIF → ACTIF)
3. **test03_changerRoleUtilisateur** : Changement de rôle (UTILISATEUR → MARCHAND)
4. **test04_utilisateurCreeDeal** : Création d'un deal par l'utilisateur
5. **test05_utilisateurParticiperAuDeal** : Participation à un deal avec paiement simulé
6. **test06_verifierParticipationCommande** : Vérification de la participation à une commande
7. **test07_recupererInformationsUtilisateur** : Récupération des infos utilisateur (GET)
8. **test08_listerDealsCreesParlUtilisateur** : Liste des deals créés
9. **test09_supprimerUtilisateur** : Suppression de l'utilisateur (DELETE)

#### Test de Parcours Complet
10. **test10_parcoursCompletUtilisateur** : Parcours E2E complet d'un utilisateur

```
Inscription → Activation → Changement rôle → Création deal → Paiement → Suppression
```

**Cas testés :**
- ✅ Création d'un utilisateur avec statut INACTIF
- ✅ Activation du compte (passage à ACTIF)
- ✅ Changement de rôle (UTILISATEUR → MARCHAND)
- ✅ Création d'un deal en tant que marchand
- ✅ Participation à un deal avec simulation de paiement Square
- ✅ Vérification de la participation à une commande
- ✅ Suppression de l'utilisateur avec cascade (deals, paiements)

**⚠️ Note importante :** Ce test ne corrige PAS les erreurs fonctionnelles. Il vérifie uniquement que les endpoints sont accessibles et retournent des codes HTTP cohérents. Les assertions métier doivent être ajustées selon l'implémentation réelle.

## 🚀 Exécution

### Tous les tests d'intégration (Commande + Utilisateur)
```bash
./mvnw test -pl modules/bff/bff-configuration -Dtest="Parcours*RestIT"
```

### Tests de parcours commande uniquement
```bash
./mvnw test -pl modules/bff/bff-configuration -Dtest=ParcoursCommandeRestIT
```

### Tests de parcours utilisateur uniquement
```bash
./mvnw test -pl modules/bff/bff-configuration -Dtest=ParcoursUtilisateurRestIT
```

### Test spécifique (commande)
```bash
./mvnw test -pl modules/bff/bff-configuration -Dtest=ParcoursCommandeRestIT#test01_creerDealViaRest
```

### Test spécifique (utilisateur)
```bash
./mvnw test -pl modules/bff/bff-configuration -Dtest=ParcoursUtilisateurRestIT#test01_creerUtilisateur
```

### Parcours complet commande uniquement
```bash
./mvnw test -pl modules/bff/bff-configuration -Dtest=ParcoursCommandeRestIT#parcoursRestComplet_dealVersCommandeTerminee
```

### Parcours complet utilisateur uniquement
```bash
./mvnw test -pl modules/bff/bff-configuration -Dtest=ParcoursUtilisateurRestIT#test10_parcoursCompletUtilisateur
```

## 📊 Résultat Attendu

### Test de parcours commande complet

Le test de parcours commande complet affiche :
```
🚀 DÉBUT DU PARCOURS REST COMPLET

✅ ÉTAPE 1 : Deal créé avec UUID xxx
✅ ÉTAPE 2.1 : Paiement acheteur 1 créé
✅ ÉTAPE 2.2 : Paiement acheteur 2 créé
✅ ÉTAPE 2.3 : Paiement acheteur 3 créé
✅ ÉTAPE 3 : Commande récupérée avec UUID yyy
✅ ÉTAPE 4 : Payout validé - Statut: PAYOUT
✅ ÉTAPE 5 : Facture uploadée - Statut: INVOICE_CUSTOMER
✅ ÉTAPE 6 : Factures validées - Statut: TERMINEE

🎉 PARCOURS COMPLET TERMINÉ AVEC SUCCÈS !

📊 Récapitulatif :
   - Deal UUID: xxx
   - Commande UUID: yyy
   - Statut final: TERMINEE
   - Nombre de participants: 3
   - Montant total: 300.00
```

### Test de parcours utilisateur complet

Le test de parcours utilisateur complet affiche :
```
🎉 PARCOURS COMPLET UTILISATEUR

✅ ÉTAPE 1/6 : Utilisateur créé - UUID: xxx
✅ ÉTAPE 2/6 : Compte activé
✅ ÉTAPE 3/6 : Rôle changé en MARCHAND
✅ ÉTAPE 4/6 : Deal créé - UUID: yyy
✅ ÉTAPE 5/6 : Paiement effectué
✅ ÉTAPE 6/6 : Utilisateur supprimé

📊 RÉCAPITULATIF DU PARCOURS :
   - Email : parcours.complet@paytogether.com
   - UUID : xxx
   - Deal créé : Oui
   - Suppression : Demandée

🎉 PARCOURS COMPLET TERMINÉ !
```

## 🎯 Avantages

✅ **Tests E2E complets** : Valident le flux métier de bout en bout  
✅ **Pas de mock complexe** : WireMock simple pour les dépendances externes  
✅ **Rapides** : H2 en mémoire + sécurité désactivée  
✅ **Isolation** : Base de données nettoyée après chaque test  
✅ **Fiabilité** : Validation à chaque étape (assertions SQL + HTTP)  
✅ **Maintenabilité** : Méthodes utilitaires réutilisables dans AbstractIT  

## 🔍 Debugging

### Voir les appels HTTP
Activer les logs RestAssured dans `application-test.yml` :
```yaml
logging:
  level:
    io.restassured: DEBUG
```

### Voir les requêtes WireMock
```java
wireMockServer.getAllServeEvents().forEach(event -> 
    System.out.println(event.getRequest())
);
```

### Inspecter la base de données
Ajouter un breakpoint après une assertion et exécuter :
```java
commandeRepository.findAll().forEach(System.out::println);
```

## 📝 Bonnes Pratiques

1. ✅ **Toujours nettoyer la base** : `@AfterEach nettoyerBaseDeDonnees()`
2. ✅ **Ordre des tests** : `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`
3. ✅ **Assertions claires** : Messages explicites dans `assertEquals()`
4. ✅ **Mock WireMock** : Réinitialiser après chaque test avec `wireMockServer.resetAll()`
5. ✅ **Fixtures réutilisables** : Créer dans `AbstractIT`, utiliser dans les tests
6. ✅ **Tests isolés** : Chaque test doit pouvoir s'exécuter seul
7. ✅ **Vérifications multiples** : HTTP + SQL pour garantir la cohérence

## 🛠️ Troubleshooting

### Erreur "Bean SecurityFilterChain not found"
➡️ Vérifier que `TestSecurityConfig` est importé dans `AbstractIT`

### Erreur "Connection refused"
➡️ Vérifier que le serveur Spring Boot démarre correctement (port disponible)

### Erreur "WireMock stub not found"
➡️ Vérifier que le mock est appelé avant le test avec `mockMinioPresignedUrl()`

### Tests échouent de manière aléatoire
➡️ Ajouter `@DirtiesContext` si le contexte Spring est corrompu entre les tests

## 📚 Documentation

- [Guide Architecture Hexagonale](../../../../../.github/copilot-instructions.md)
- [Guide Tests d'Intégration](../../../../../.github/documentation/TESTS_INTEGRATION.md)
- [WireMock Documentation](https://wiremock.org/docs/)
- [RestAssured Documentation](https://rest-assured.io/)

---

**Date de dernière mise à jour** : 15 mars 2026  
**Auteur** : Équipe PayToGether

