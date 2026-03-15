package com.ulr.paytogether.configuration.test.integration;

import com.ulr.paytogether.configuration.test.AbstractIT;
import com.ulr.paytogether.provider.adapter.entity.CommandeJpa;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Tests d'intégration REST pour SquarePaymentResource
 * Teste les endpoints de paiement Square
 */
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests d'intégration REST : SquarePaymentResource")
class SquarePaymentResourceIT extends AbstractIT {

    private DealJpa dealTest;
    private CommandeJpa commandeTest;

    @BeforeEach
    void setUpDeal() {
        dealTest = creerDealAvecImages(vendeur, categorieElectronique, 3, BigDecimal.valueOf(100.00));
        commandeTest = creerCommande(dealTest, acheteur1);
        mockSquarePaymentSuccess();
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/square-payments - Devrait créer un paiement Square")
    void creerPaiementSquare_DevraitCreerAvecSucces() {
        // Given
        Map<String, Object> adressePayload = new HashMap<>();
        adressePayload.put("rue", "123 Test Street");
        adressePayload.put("ville", "Montreal");
        adressePayload.put("codePostal", "H1A 1A1");
        adressePayload.put("numeroPhone", "+15551234567");

        Map<String, Object> paiementPayload = new HashMap<>();
        paiementPayload.put("dealUuid", dealTest.getUuid().toString());
        paiementPayload.put("utilisateurUuid", acheteur1.getUuid().toString());
        paiementPayload.put("nombreDePart", 1);
        paiementPayload.put("montant", 125);
        paiementPayload.put("methodePaiement", "SQUARE_CARD");
        paiementPayload.put("squareToken", "cnon:test_square_token_123");
        paiementPayload.put("adresse", adressePayload);

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(paiementPayload)
            .when()
            .post("/square-payments")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .body("uuid", notNullValue())
            .body("statut", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/square-payments/{uuid}/status - Devrait vérifier le statut")
    void verifierStatutPaiement_DevraitRetournerStatut() {
        // Given - Créer un paiement d'abord
        Map<String, Object> adressePayload = new HashMap<>();
        adressePayload.put("rue", "123 Test Street");
        adressePayload.put("ville", "Montreal");
        adressePayload.put("codePostal", "H1A 1A1");
        adressePayload.put("numeroPhone", "+15551234567");

        Map<String, Object> paiementPayload = new HashMap<>();
        paiementPayload.put("dealUuid", dealTest.getUuid().toString());
        paiementPayload.put("utilisateurUuid", acheteur1.getUuid().toString());
        paiementPayload.put("nombreDePart", 1);
        paiementPayload.put("montant", 125);
        paiementPayload.put("methodePaiement", "SQUARE_CARD");
        paiementPayload.put("squareToken", "cnon:test_token_verify");
        paiementPayload.put("adresse", adressePayload);

        String paiementUuid = given()
            .contentType(ContentType.JSON)
            .body(paiementPayload)
            .when()
            .post("/square-payments")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract()
            .path("uuid");

        // When & Then - Vérifier le statut
        given()
            .when()
            .get("/square-payments/" + paiementUuid + "/status")
            .then()
            .statusCode(200)
            .body("uuid", equalTo(paiementUuid))
            .body("statut", notNullValue());
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/square-payments - Devrait retourner erreur si deal inexistant")
    void creerPaiementSquare_DevraitRetournerErreurSiDealInexistant() {
        // Given
        Map<String, Object> adressePayload = new HashMap<>();
        adressePayload.put("rue", "123 Test Street");
        adressePayload.put("ville", "Montreal");
        adressePayload.put("codePostal", "H1A 1A1");
        adressePayload.put("numeroPhone", "+15551234567");

        Map<String, Object> paiementPayload = new HashMap<>();
        paiementPayload.put("dealUuid", java.util.UUID.randomUUID().toString());
        paiementPayload.put("utilisateurUuid", acheteur1.getUuid().toString());
        paiementPayload.put("nombreDePart", 1);
        paiementPayload.put("montant", 125);
        paiementPayload.put("methodePaiement", "SQUARE_CARD");
        paiementPayload.put("squareToken", "cnon:test_token");
        paiementPayload.put("adresse", adressePayload);

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(paiementPayload)
            .when()
            .post("/square-payments")
            .then()
            .statusCode(anyOf(is(400), is(404), is(500)));
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/square-payments - Devrait valider les données obligatoires")
    void creerPaiementSquare_DevraitValiderDonneesObligatoires() {
        // Given - Payload incomplet (sans adresse)
        Map<String, Object> paiementPayload = new HashMap<>();
        paiementPayload.put("dealUuid", dealTest.getUuid().toString());
        paiementPayload.put("utilisateurUuid", acheteur1.getUuid().toString());
        paiementPayload.put("nombreDePart", 1);
        paiementPayload.put("montant", 125);
        paiementPayload.put("methodePaiement", "SQUARE_CARD");
        paiementPayload.put("squareToken", "cnon:test_token");
        // Pas d'adresse

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(paiementPayload)
            .when()
            .post("/square-payments")
            .then()
            .statusCode(anyOf(is(400), is(500))); // Validation ou erreur métier
    }
}

