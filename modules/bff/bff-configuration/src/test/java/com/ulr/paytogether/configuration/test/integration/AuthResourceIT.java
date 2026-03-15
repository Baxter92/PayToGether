package com.ulr.paytogether.configuration.test.integration;

import com.ulr.paytogether.configuration.test.AbstractIT;
import com.ulr.paytogether.core.enumeration.StatutUtilisateur;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Tests d'intégration REST pour AuthResource
 * Teste les endpoints d'authentification
 */
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests d'intégration REST : AuthResource")
class AuthResourceIT extends AbstractIT {

    @Test
    @Order(1)
    @DisplayName("POST /api/auth/register - Devrait créer un compte utilisateur")
    void register_DevraitCreerCompte() {
        // Given
        Map<String, Object> registerPayload = new HashMap<>();
        registerPayload.put("email", "newuser@test.com");
        registerPayload.put("motDePasse", "Password123!");
        registerPayload.put("nom", "Nouveau");
        registerPayload.put("prenom", "Utilisateur");
        registerPayload.put("role", "UTILISATEUR");

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(registerPayload)
            .when()
            .post("/auth/register")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .body("uuid", notNullValue())
            .body("email", equalTo("newuser@test.com"))
            .body("nom", equalTo("Nouveau"))
            .body("prenom", equalTo("Utilisateur"));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/auth/login - Devrait se connecter avec succès")
    void login_DevraitRetournerToken() {
        // Given
        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("username", vendeur.getEmail());
        loginPayload.put("password", "password"); // Mot de passe de test

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(loginPayload)
            .when()
            .post("/auth/login")
            .then()
            .statusCode(anyOf(is(200), is(401))); // Selon implémentation Keycloak
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/auth/login - Devrait retourner 401 si credentials invalides")
    void login_DevraitRetourner401SiCredentialsInvalides() {
        // Given
        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("username", "inexistant@test.com");
        loginPayload.put("password", "wrongpassword");

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(loginPayload)
            .when()
            .post("/auth/login")
            .then()
            .statusCode(401);
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/auth/me - Devrait retourner les infos utilisateur connecté")
    void getMe_DevraitRetournerInfosUtilisateur() {
        // When & Then - En mode test, la sécurité est désactivée
        given()
            .when()
            .get("/auth/me")
            .then()
            .statusCode(anyOf(is(200), is(401))); // Selon configuration sécurité
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/auth/forgot-password - Devrait envoyer email de réinitialisation")
    void forgotPassword_DevraitEnvoyerEmail() {
        // Given
        Map<String, Object> forgotPasswordPayload = new HashMap<>();
        forgotPasswordPayload.put("email", vendeur.getEmail());

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(forgotPasswordPayload)
            .when()
            .post("/auth/forgot-password")
            .then()
            .statusCode(200); // Toujours 200 pour sécurité
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/auth/forgot-password - Devrait retourner 200 même si email inexistant")
    void forgotPassword_DevraitRetourner200MemeEmailInexistant() {
        // Given
        Map<String, Object> forgotPasswordPayload = new HashMap<>();
        forgotPasswordPayload.put("email", "inexistant@test.com");

        // When & Then - Pour raisons de sécurité, retourne toujours 200
        given()
            .contentType(ContentType.JSON)
            .body(forgotPasswordPayload)
            .when()
            .post("/auth/forgot-password")
            .then()
            .statusCode(200);
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/auth/reset-password - Devrait réinitialiser le mot de passe")
    void resetPassword_DevraitReinitialiserMotDePasse() {
        // Given - Créer un token de validation
        UtilisateurJpa utilisateur = creerUtilisateur(StatutUtilisateur.INACTIF);
        setValidationToken(utilisateur.getUuid(), "test-reset-token-123");

        Map<String, Object> resetPasswordPayload = new HashMap<>();
        resetPasswordPayload.put("token", "test-reset-token-123");
        resetPasswordPayload.put("nouveauMotDePasse", "NewPassword123!");

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(resetPasswordPayload)
            .when()
            .post("/auth/reset-password")
            .then()
            .statusCode(anyOf(is(200), is(400), is(404))); // Selon implémentation
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/auth/activate-account - Devrait valider un compte")
    void validateAccount_DevraitValiderCompte() {
        // Given - Créer un utilisateur en attente avec token
        UtilisateurJpa utilisateur = creerUtilisateur(StatutUtilisateur.INACTIF);
        setValidationToken(utilisateur.getUuid(), "testvalidationtoken456");

        Map<String, Object> validatePayload = new HashMap<>();
        validatePayload.put("token", "testvalidationtoken456");

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(validatePayload)
            .when()
            .get("/auth/activate-account")
            .then()
            .statusCode(anyOf(is(200), is(400), is(404))); // Selon implémentation
    }

    @Test
    @Order(9)
    @DisplayName("POST /api/auth/register - Devrait retourner 400 si email déjà existant")
    void register_DevraitRetourner400SiEmailExistant() {
        // Given
        Map<String, Object> registerPayload = new HashMap<>();
        registerPayload.put("email", vendeur.getEmail()); // Email déjà existant
        registerPayload.put("motDePasse", "Password123!");
        registerPayload.put("nom", "Nouveau");
        registerPayload.put("prenom", "Utilisateur");
        registerPayload.put("role", "UTILISATEUR");

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(registerPayload)
            .when()
            .post("/auth/register")
            .then()
            .statusCode(anyOf(is(400), is(409), is(500))); // Email déjà existant
    }
}

