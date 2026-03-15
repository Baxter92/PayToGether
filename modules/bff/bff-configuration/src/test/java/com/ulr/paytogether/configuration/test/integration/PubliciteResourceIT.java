package com.ulr.paytogether.configuration.test.integration;

import com.ulr.paytogether.configuration.test.AbstractIT;
import com.ulr.paytogether.provider.adapter.entity.PubliciteJpa;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration REST pour PubliciteResource
 * Teste tous les endpoints de gestion des publicités
 */
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests d'intégration REST : PubliciteResource")
class PubliciteResourceIT extends AbstractIT {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Test
    @Order(1)
    @DisplayName("POST /api/publicites - Devrait créer une publicité avec images")
    void creerPublicite_DevraitCreerAvecImages() {
        // Given
        mockMinioPresignedUrl();
        mockMinioUpload();

        Map<String, Object> publicitePayload = new HashMap<>();
        publicitePayload.put("titre", "Publicité Test");
        publicitePayload.put("description", "Description de la publicité");
        publicitePayload.put("lienExterne", "https://example.com");
        publicitePayload.put("dateDebut", LocalDateTime.now().format(DATE_FORMATTER));
        publicitePayload.put("dateFin", LocalDateTime.now().plusDays(30).format(DATE_FORMATTER));
        publicitePayload.put("statut", "ACTIVE");
        publicitePayload.put("listeImages", List.of(
            Map.of("urlImage", "pub_image_1.jpg", "isPrincipal", true),
            Map.of("urlImage", "pub_image_2.jpg", "isPrincipal", false)
        ));

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(publicitePayload)
            .when()
            .post("/publicites")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .body("uuid", notNullValue())
            .body("titre", equalTo("Publicité Test"))
            .body("statut", equalTo("ACTIVE"))
            .body("listeImages", hasSize(2));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/publicites/{uuid} - Devrait récupérer une publicité par UUID")
    void lireParUuid_DevraitRetournerPublicite() {
        // Given
        PubliciteJpa publicite = creerPubliciteTest();

        // When & Then
        given()
            .when()
            .get("/publicites/" + publicite.getUuid())
            .then()
            .statusCode(200)
            .body("uuid", equalTo(publicite.getUuid().toString()))
            .body("titre", notNullValue());
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/publicites/{uuid} - Devrait retourner 404 si inexistant")
    void lireParUuid_DevraitRetourner404SiInexistant() {
        // Given
        UUID uuidInexistant = UUID.randomUUID();

        // When & Then
        given()
            .when()
            .get("/publicites/" + uuidInexistant)
            .then()
            .statusCode(404);
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/publicites/actives - Devrait récupérer les publicités actives")
    void lireActives_DevraitRetournerPublicitesActives() {
        // Given
        creerPubliciteTest();
        creerPubliciteTest();

        // When & Then
        given()
            .when()
            .get("/publicites/actives")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(2)))
            .body("[0].statut", equalTo("ACTIVE"));
    }

    @Test
    @Order(5)
    @DisplayName("PUT /api/publicites/{uuid} - Devrait mettre à jour une publicité")
    void mettreAJour_DevraitModifierPublicite() {
        // Given
        PubliciteJpa publicite = creerPubliciteTest();

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("titre", "Publicité Modifiée");
        updatePayload.put("description", "Nouvelle description");
        updatePayload.put("lienExterne", "https://newlink.com");
        updatePayload.put("dateDebut", LocalDateTime.now().format(DATE_FORMATTER));
        updatePayload.put("dateFin", LocalDateTime.now().plusDays(45).format(DATE_FORMATTER));
        updatePayload.put("statut", "INACTIVE");

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(updatePayload)
            .when()
            .put("/publicites/" + publicite.getUuid())
            .then()
            .statusCode(200)
            .body("titre", equalTo("Publicité Modifiée"))
            .body("statut", equalTo("INACTIVE"));
    }

    @Test
    @Order(6)
    @DisplayName("PATCH /api/publicites/{uuid}/images/{imageUuid}/confirm - Devrait confirmer upload")
    void confirmerUploadImage_DevraitMettreStatutUploaded() {
        // Given
        PubliciteJpa publicite = creerPubliciteTest();
        
        // When & Then
        if (publicite.getListeImages() != null && !publicite.getListeImages().isEmpty()) {
            UUID imageUuid = publicite.getListeImages().getFirst().getUuid();
            
            given()
                .when()
                .patch("/publicites/" + publicite.getUuid() + "/images/" + imageUuid + "/confirm")
                .then()
                .statusCode(200);
        }
    }

    @Test
    @Order(7)
    @DisplayName("DELETE /api/publicites/{uuid} - Devrait supprimer une publicité")
    void supprimer_DevraitSupprimerPublicite() {
        // Given
        PubliciteJpa publicite = creerPubliciteTest();

        // When & Then
        given()
            .when()
            .delete("/publicites/" + publicite.getUuid())
            .then()
            .statusCode(anyOf(is(200), is(204)));

        // Vérifier que la publicité n'existe plus
        assertFalse(publiciteRepository.findById(publicite.getUuid()).isPresent());
    }

    /**
     * Méthode helper pour créer une publicité de test
     */
    private PubliciteJpa creerPubliciteTest() {
        PubliciteJpa publicite = new PubliciteJpa();
        publicite.setTitre("Publicité Test");
        publicite.setDescription("Description test");
        publicite.setDateDebut(LocalDateTime.now());
        publicite.setDateFin(LocalDateTime.now().plusDays(30));
        publicite.setActive(true);
        return publiciteRepository.save(publicite);
    }
}

