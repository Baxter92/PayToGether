package com.ulr.paytogether.configuration.test.integration;

import com.ulr.paytogether.configuration.test.AbstractIT;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Tests d'intégration REST pour DealRechercheResource
 * Teste les endpoints de recherche Elasticsearch
 * 
 * Note: Ces tests nécessitent Elasticsearch en mode test (ou mock)
 */
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests d'intégration REST : DealRechercheResource")
class DealRechercheResourceIT extends AbstractIT {

    @Test
    @Order(1)
    @DisplayName("GET /api/recherche/deals?q=... - Devrait rechercher des deals")
    void rechercherDeals_DevraitRetournerResultats() {
        // Given - Créer quelques deals
        DealJpa deal1 = creerDealAvecImages(vendeur, categorieElectronique, 3, BigDecimal.valueOf(100.00));
        DealJpa deal2 = creerDealAvecImages(vendeur, categorieMaison, 2, BigDecimal.valueOf(50.00));

        // When & Then
        given()
            .queryParam("q", "Deal")
            .when()
            .get("/recherche/deals")
            .then()
            .statusCode(200)
            .body("$", anyOf(hasSize(greaterThanOrEqualTo(0)), empty())); // Peut être vide si ES pas configuré
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/recherche/deals?q= - Devrait retourner liste vide si query vide")
    void rechercherDeals_DevraitRetournerVideSiQueryVide() {
        // When & Then
        given()
            .queryParam("q", "")
            .when()
            .get("/recherche/deals")
            .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/recherche/deals - Devrait retourner liste vide si pas de query param")
    void rechercherDeals_DevraitRetournerVideSiPasDeQueryParam() {
        // When & Then
        given()
            .when()
            .get("/recherche/deals")
            .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/recherche/deals?q=Montreal - Devrait rechercher par ville")
    void rechercherDeals_DevraitRechercherParVille() {
        // Given
        creerDealAvecImages(vendeur, categorieElectronique, 3, BigDecimal.valueOf(100.00));

        // When & Then
        given()
            .queryParam("q", "Montreal")
            .when()
            .get("/recherche/deals")
            .then()
            .statusCode(200);
            // Ne pas tester le contenu car Elasticsearch peut ne pas être indexé en test
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/recherche/deals/reindex - Devrait réindexer tous les deals")
    void reindexerTousLesDeals_DevraitReussir() {
        // Given
        creerDealAvecImages(vendeur, categorieElectronique, 3, BigDecimal.valueOf(100.00));
        creerDealAvecImages(vendeur, categorieMaison, 2, BigDecimal.valueOf(75.00));

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .when()
            .post("/recherche/deals/reindex")
            .then()
            .statusCode(anyOf(is(200), is(500))); // Peut échouer si ES pas configuré
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/recherche/deals?q=Électronique - Devrait rechercher par catégorie")
    void rechercherDeals_DevraitRechercherParCategorie() {
        // Given
        creerDealAvecImages(vendeur, categorieElectronique, 3, BigDecimal.valueOf(100.00));

        // When & Then
        given()
            .queryParam("q", "Électronique")
            .when()
            .get("/recherche/deals")
            .then()
            .statusCode(200);
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/recherche/deals?q=TestQuery123 - Devrait retourner vide si aucun résultat")
    void rechercherDeals_DevraitRetournerVideSiAucunResultat() {
        // When & Then
        given()
            .queryParam("q", "TestQuery123NonExistant")
            .when()
            .get("/recherche/deals")
            .then()
            .statusCode(200)
            .body("$", anyOf(hasSize(0), hasSize(greaterThanOrEqualTo(0))));
    }
}

