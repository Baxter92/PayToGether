package com.ulr.paytogether.configuration.test.integration;

import com.ulr.paytogether.configuration.test.AbstractIT;
import com.ulr.paytogether.provider.adapter.entity.CategorieJpa;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration REST pour CategorieResource
 * Teste tous les endpoints de gestion des catégories
 */
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests d'intégration REST : CategorieResource")
class CategorieResourceIT extends AbstractIT {

    @Test
    @Order(1)
    @DisplayName("POST /api/categories - Devrait créer une catégorie")
    void creerCategorie_DevraitCreerAvecSucces() {
        // Given
        Map<String, Object> categoriePayload = new HashMap<>();
        categoriePayload.put("nom", "Sports & Loisirs");
        categoriePayload.put("description", "Catégorie pour articles de sport");

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(categoriePayload)
            .when()
            .post("/categories")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .body("uuid", notNullValue())
            .body("nom", equalTo("Sports & Loisirs"))
            .body("description", equalTo("Catégorie pour articles de sport"));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/categories/{uuid} - Devrait récupérer une catégorie par UUID")
    void lireParUuid_DevraitRetournerCategorie() {
        // Given - categorieElectronique est créé dans AbstractIT

        // When & Then
        given()
            .when()
            .get("/categories/" + categorieElectronique.getUuid())
            .then()
            .statusCode(200)
            .body("uuid", equalTo(categorieElectronique.getUuid().toString()))
            .body("nom", equalTo("Électronique"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/categories/{uuid} - Devrait retourner 404 si inexistant")
    void lireParUuid_DevraitRetourner404SiInexistant() {
        // Given
        UUID uuidInexistant = UUID.randomUUID();

        // When & Then
        given()
            .when()
            .get("/categories/" + uuidInexistant)
            .then()
            .statusCode(404);
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/categories/nom/{nom} - Devrait récupérer une catégorie par nom")
    void lireParNom_DevraitRetournerCategorie() {
        // Given - categorieElectronique est créé dans AbstractIT

        // When & Then
        given()
            .when()
            .get("/categories/nom/Électronique")
            .then()
            .statusCode(200)
            .body("nom", equalTo("Électronique"));
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/categories - Devrait récupérer toutes les catégories")
    void lireTous_DevraitRetournerToutesLesCategories() {
        // Given - categorieElectronique et categorieMaison sont créées dans AbstractIT

        // When & Then
        given()
            .when()
            .get("/categories")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(2)))
            .body("[0].nom", notNullValue())
            .body("[0].description", notNullValue());
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/categories/existe/{nom} - Devrait vérifier si une catégorie existe")
    void existe_DevraitRetournerTrue() {
        // Given - categorieElectronique existe

        // When & Then
        given()
            .when()
            .get("/categories/existe/Électronique")
            .then()
            .statusCode(200)
            .body(equalTo("true"));
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/categories/existe/{nom} - Devrait retourner false si inexistant")
    void existe_DevraitRetournerFalseSiInexistant() {
        // Given
        String nomInexistant = "Catégorie Inexistante";

        // When & Then
        given()
            .when()
            .get("/categories/existe/" + nomInexistant)
            .then()
            .statusCode(200)
            .body(equalTo("false"));
    }

    @Test
    @Order(8)
    @DisplayName("PUT /api/categories/{uuid} - Devrait mettre à jour une catégorie")
    void mettreAJour_DevraitModifierCategorie() {
        // Given
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("nom", "Électronique & High-Tech");
        updatePayload.put("description", "Catégorie mise à jour");

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(updatePayload)
            .when()
            .put("/categories/" + categorieElectronique.getUuid())
            .then()
            .statusCode(200)
            .body("nom", equalTo("Électronique & High-Tech"))
            .body("description", equalTo("Catégorie mise à jour"));
    }

    @Test
    @Order(9)
    @DisplayName("DELETE /api/categories/{uuid} - Devrait supprimer une catégorie")
    void supprimer_DevraitSupprimerCategorie() {
        // Given - Créer une catégorie temporaire pour la supprimer
        CategorieJpa categorieTemp = CategorieJpa.builder()
            .nom("Catégorie à supprimer")
            .description("Description temporaire")
            .build();
        categorieTemp = categorieRepository.save(categorieTemp);

        // When & Then
        given()
            .when()
            .delete("/categories/" + categorieTemp.getUuid())
            .then()
            .statusCode(anyOf(is(200), is(204)));

        // Vérifier que la catégorie n'existe plus
        assertFalse(categorieRepository.findById(categorieTemp.getUuid()).isPresent());
    }

    @Test
    @Order(10)
    @DisplayName("POST /api/categories - Devrait retourner 400 si nom déjà existant")
    void creerCategorie_DevraitRetourner400SiNomExistant() {
        // Given
        Map<String, Object> categoriePayload = new HashMap<>();
        categoriePayload.put("nom", "Électronique"); // Nom déjà existant
        categoriePayload.put("description", "Description test");

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(categoriePayload)
            .when()
            .post("/categories")
            .then()
            .statusCode(400);
    }
}

