# Mocks WireMock pour Keycloak

Ce dossier contient les fichiers JSON pour simuler les réponses de l'API Keycloak lors des tests d'intégration.

## 📁 Structure

```
wiremock/
├── keycloak/                    # Réponses JSON de Keycloak
│   ├── admin-token-success.json    # Token admin OAuth2
│   ├── user-created.json           # Utilisateur créé
│   ├── user-conflict.json          # Erreur email déjà existant
│   ├── user-details.json           # Détails d'un utilisateur
│   ├── user-not-found.json         # Utilisateur non trouvé
│   └── roles-list.json             # Liste des rôles disponibles
│
└── mappings/                    # Mappings WireMock (requêtes → réponses)
    ├── keycloak-admin-auth.json     # POST /realms/master/protocol/openid-connect/token
    ├── keycloak-create-user.json    # POST /admin/realms/{realm}/users
    ├── keycloak-get-user.json       # GET  /admin/realms/{realm}/users/{userId}
    ├── keycloak-update-user.json    # PUT  /admin/realms/{realm}/users/{userId}
    ├── keycloak-delete-user.json    # DELETE /admin/realms/{realm}/users/{userId}
    ├── keycloak-get-roles.json      # GET  /admin/realms/{realm}/roles
    ├── keycloak-assign-role.json    # POST /admin/realms/{realm}/users/{userId}/role-mappings/realm
    └── keycloak-remove-role.json    # DELETE /admin/realms/{realm}/users/{userId}/role-mappings/realm
```

## 🔧 Configuration Automatique

Les mocks Keycloak sont **chargés automatiquement** par WireMock au démarrage des tests.

**Aucune méthode mock n'est nécessaire dans AbstractIT** - WireMock lit automatiquement :
1. Les **mappings** depuis `src/test/resources/wiremock/mappings/`
2. Les **réponses** depuis `src/test/resources/wiremock/keycloak/`

## 🧪 Endpoints Mockés

### 1. Authentification Admin
**Endpoint** : `POST /realms/master/protocol/openid-connect/token`  
**Réponse** : `keycloak/admin-token-success.json`  
**Statut** : 200 OK

Retourne un token d'accès OAuth2 pour l'admin Keycloak.

### 2. Création d'Utilisateur
**Endpoint** : `POST /admin/realms/{realm}/users`  
**Réponse** : Header `Location` avec l'URL du nouvel utilisateur  
**Statut** : 201 Created

Simule la création d'un utilisateur dans Keycloak.

### 3. Récupération d'Utilisateur
**Endpoint** : `GET /admin/realms/{realm}/users/{userId}`  
**Réponse** : `keycloak/user-details.json`  
**Statut** : 200 OK

Retourne les détails d'un utilisateur Keycloak.

### 4. Mise à Jour d'Utilisateur
**Endpoint** : `PUT /admin/realms/{realm}/users/{userId}`  
**Réponse** : Aucune (No Content)  
**Statut** : 204 No Content

Simule la mise à jour d'un utilisateur (statut, rôle, etc.).

### 5. Suppression d'Utilisateur
**Endpoint** : `DELETE /admin/realms/{realm}/users/{userId}`  
**Réponse** : Aucune (No Content)  
**Statut** : 204 No Content

Simule la suppression d'un utilisateur dans Keycloak.

### 6. Liste des Rôles
**Endpoint** : `GET /admin/realms/{realm}/roles`  
**Réponse** : `keycloak/roles-list.json`  
**Statut** : 200 OK

Retourne la liste des rôles disponibles :
- `ADMIN`
- `UTILISATEUR`
- `VENDEUR`

### 7. Assigner un Rôle
**Endpoint** : `POST /admin/realms/{realm}/users/{userId}/role-mappings/realm`  
**Réponse** : Aucune (No Content)  
**Statut** : 204 No Content

Simule l'assignation d'un rôle à un utilisateur.

### 8. Retirer un Rôle
**Endpoint** : `DELETE /admin/realms/{realm}/users/{userId}/role-mappings/realm`  
**Réponse** : Aucune (No Content)  
**Statut** : 204 No Content

Simule le retrait d'un rôle d'un utilisateur.

## 📝 Utilisation dans les Tests

### Exemple : Test de Création d'Utilisateur

```java
@Test
void test_creerUtilisateur() {
    // Les mocks Keycloak sont automatiques via WireMock mappings JSON
    
    Map<String, Object> payload = new HashMap<>();
    payload.put("nom", "Test");
    payload.put("prenom", "User");
    payload.put("email", "test@paytogether.com");
    payload.put("motDePasse", "SecurePass123!");

    String userId = given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/auth/register")
            .then()
            .statusCode(201)
            .body("uuid", notNullValue())
            .extract()
            .path("uuid");

    // ✅ WireMock intercepte automatiquement :
    // 1. POST /realms/master/protocol/openid-connect/token → Authentification admin
    // 2. POST /admin/realms/paytogether/users → Création utilisateur
}
```

### Exemple : Test de Changement de Rôle

```java
@Test
void test_changerRole() {
    // Les mocks Keycloak sont automatiques via WireMock mappings JSON
    
    Map<String, Object> payload = new HashMap<>();
    payload.put("role", "VENDEUR");

    given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .patch("/admin/utilisateurs/" + userId + "/role")
            .then()
            .statusCode(200);

    // ✅ WireMock intercepte automatiquement :
    // 1. GET /admin/realms/paytogether/roles → Liste des rôles
    // 2. POST /admin/realms/paytogether/users/{id}/role-mappings/realm → Assigner rôle
    // 3. DELETE /admin/realms/paytogether/users/{id}/role-mappings/realm → Retirer ancien rôle
}
```

## 🔍 Vérification des Mocks

Pour vérifier que les mocks sont bien chargés, démarrer les tests et consulter les logs WireMock :

```
WireMock started on port 12345
Loaded 8 stub mappings from classpath:
  - keycloak-admin-auth.json
  - keycloak-create-user.json
  - keycloak-get-user.json
  - keycloak-update-user.json
  - keycloak-delete-user.json
  - keycloak-get-roles.json
  - keycloak-assign-role.json
  - keycloak-remove-role.json
```

## 🎯 Avantages

### ✅ Pas de Code dans AbstractIT
- Toutes les réponses sont dans des fichiers JSON
- Pas de méthode `mockKeycloakXxx()` à maintenir
- Code de test plus propre

### ✅ Mocks Automatiques
- WireMock charge les mappings au démarrage
- Pas besoin d'appeler `mockKeycloakAdminAuth()` dans chaque test
- Moins de code répétitif

### ✅ Facilité de Maintenance
- Modifier une réponse = éditer un fichier JSON
- Ajouter un nouveau mock = ajouter 2 fichiers (mapping + réponse)
- Pas besoin de recompiler

### ✅ Réutilisabilité
- Les mêmes fichiers JSON peuvent être utilisés pour tous les tests
- Pas de duplication de code

## 🛠️ Ajouter un Nouveau Mock

1. **Créer la réponse JSON** dans `wiremock/keycloak/`
   ```json
   // new-response.json
   {
     "data": "example"
   }
   ```

2. **Créer le mapping** dans `wiremock/mappings/`
   ```json
   // keycloak-new-endpoint.json
   {
     "request": {
       "method": "GET",
       "urlPathPattern": "/admin/realms/.*/new-endpoint"
     },
     "response": {
       "status": 200,
       "headers": {
         "Content-Type": "application/json"
       },
       "bodyFileName": "keycloak/new-response.json"
     }
   }
   ```

3. **Relancer les tests** - WireMock charge automatiquement le nouveau mock !

## 📚 Documentation WireMock

- [WireMock Documentation](http://wiremock.org/docs/)
- [Request Matching](http://wiremock.org/docs/request-matching/)
- [Response Templating](http://wiremock.org/docs/response-templating/)

---

**Date de création** : 15 mars 2026  
**Auteur** : Équipe PayToGether  
**Statut** : ✅ Prêt à utiliser

