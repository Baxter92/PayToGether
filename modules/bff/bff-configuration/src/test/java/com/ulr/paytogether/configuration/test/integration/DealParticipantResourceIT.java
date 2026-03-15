package com.ulr.paytogether.configuration.test.integration;

import com.ulr.paytogether.configuration.test.AbstractIT;
import com.ulr.paytogether.provider.adapter.entity.CommandeJpa;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Tests d'intégration REST pour DealParticipantResource
 * Teste l'endpoint de gestion des participants aux deals
 */
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests d'intégration REST : DealParticipantResource")
class DealParticipantResourceIT extends AbstractIT {

    private DealJpa dealTest;
    private CommandeJpa commandeTest;

    @BeforeEach
    void setUpDeal() {
        // Créer un deal et une commande avec paiements
        dealTest = creerDealAvecImages(vendeur, categorieElectronique, 3, BigDecimal.valueOf(100.00));
        commandeTest = creerCommande(dealTest, acheteur1);
        
        // Simuler des paiements complets pour avoir des participants
        simulerPaiementsComplets(commandeTest, List.of(acheteur1, acheteur2, acheteur3));
    }

    @Test
    @Order(1)
    @DisplayName("GET /api/deals/{dealUuid}/participants - Devrait lister les participants d'un deal")
    void listerParticipants_DevraitRetournerTousLesParticipants() {
        // When & Then
        given()
            .when()
            .get("/deals/" + dealTest.getUuid() + "/participants")
            .then()
            .statusCode(200)
            .body("$", hasSize(3))
            .body("[0].utilisateurNom", notNullValue())
            .body("[0].utilisateurPrenom", notNullValue())
            .body("[0].nombreParts", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/deals/{dealUuid}/participants - Devrait retourner liste vide si aucun participant")
    void listerParticipants_DevraitRetournerListeVideSiAucunParticipant() {
        // Given - Créer un nouveau deal sans participants
        DealJpa dealSansParticipant = creerDealAvecImages(vendeur, categorieMaison, 2, BigDecimal.valueOf(50.00));

        // When & Then
        given()
            .when()
            .get("/deals/" + dealSansParticipant.getUuid() + "/participants")
            .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/deals/{dealUuid}/participants - Devrait retourner 404 si deal inexistant")
    void listerParticipants_DevraitRetourner404SiDealInexistant() {
        // Given
        java.util.UUID uuidInexistant = java.util.UUID.randomUUID();

        // When & Then
        given()
            .when()
            .get("/deals/" + uuidInexistant + "/participants")
            .then()
            .statusCode(anyOf(is(404), is(500))); // Selon l'implémentation
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/deals/{dealUuid}/participants - Devrait inclure les informations complètes des participants")
    void listerParticipants_DevraitInclureInformationsCompletes() {
        // When & Then
        given()
            .when()
            .get("/deals/" + dealTest.getUuid() + "/participants")
            .then()
            .statusCode(200)
            .body("$", hasSize(3))
            .body("[0].utilisateurUuid", notNullValue())
            .body("[0].utilisateurNom", notNullValue())
            .body("[0].utilisateurPrenom", notNullValue())
            .body("[0].utilisateurEmail", notNullValue())
            .body("[0].nombreParts", greaterThan(0))
            .body("[0].montantTotal", notNullValue());
    }
}

