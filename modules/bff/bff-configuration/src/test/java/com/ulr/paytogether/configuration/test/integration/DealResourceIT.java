package com.ulr.paytogether.configuration.test.integration;

import com.ulr.paytogether.configuration.test.AbstractIT;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
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
 * Tests d'intégration REST pour DealResource
 * Teste tous les endpoints de gestion des deals
 */
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests d'intégration REST : DealResource")
class DealResourceIT extends AbstractIT {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final BigDecimal PRIX_PART = BigDecimal.valueOf(100.00);
    private static final int NB_PARTICIPANTS = 3;

    @Test
    @Order(1)
    @DisplayName("POST /api/deals - Devrait créer un deal avec images")
    void creerDeal_DevraitCreerAvecImages() {
        // Given
        mockMinioPresignedUrl();
        mockMinioUpload();

        Map<String, Object> dealPayload = new HashMap<>();
        dealPayload.put("titre", "Deal Integration Test");
        dealPayload.put("description", "Description du deal de test");
        dealPayload.put("prixDeal", PRIX_PART.multiply(BigDecimal.valueOf(NB_PARTICIPANTS)));
        dealPayload.put("prixPart", PRIX_PART);
        dealPayload.put("nbParticipants", NB_PARTICIPANTS);
        dealPayload.put("dateDebut", LocalDateTime.now().format(DATE_FORMATTER));
        dealPayload.put("dateFin", LocalDateTime.now().plusDays(30).format(DATE_FORMATTER));
        dealPayload.put("dateExpiration", LocalDateTime.now().plusDays(30).format(DATE_FORMATTER));
        dealPayload.put("ville", "Montreal");
        dealPayload.put("pays", "Canada");
        dealPayload.put("statut", "PUBLIE");
        dealPayload.put("createurUuid", vendeur.getUuid().toString());
        dealPayload.put("categorieUuid", categorieElectronique.getUuid().toString());
        dealPayload.put("listePointsForts", List.of("Point fort 1", "Point fort 2"));
        dealPayload.put("listeImages", List.of(
            Map.of("urlImage", "test_image_1.jpg", "isPrincipal", true),
            Map.of("urlImage", "test_image_2.jpg", "isPrincipal", false)
        ));

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(dealPayload)
            .when()
            .post("/deals")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .body("uuid", notNullValue())
            .body("titre", equalTo("Deal Integration Test"))
            .body("statut", equalTo("PUBLIE"))
            .body("nbParticipants", equalTo(NB_PARTICIPANTS))
            .body("listeImages", hasSize(2));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/deals/{uuid} - Devrait récupérer un deal par UUID")
    void lireParUuid_DevraitRetournerDeal() {
        // Given
        DealJpa deal = creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);

        // When & Then
        given()
            .when()
            .get("/deals/" + deal.getUuid())
            .then()
            .statusCode(200)
            .body("uuid", equalTo(deal.getUuid().toString()))
            .body("titre", notNullValue())
            .body("statut", equalTo("PUBLIE"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/deals/{uuid} - Devrait retourner 404 si deal inexistant")
    void lireParUuid_DevraitRetourner404SiInexistant() {
        // Given
        UUID uuidInexistant = UUID.randomUUID();

        // When & Then
        given()
            .when()
            .get("/deals/" + uuidInexistant)
            .then()
            .statusCode(404);
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/deals/statut/{statut} - Devrait récupérer deals par statut")
    void lireParStatut_DevraitRetournerDealsPublies() {
        // Given
        creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);
        creerDealAvecImages(vendeur, categorieMaison, 2, BigDecimal.valueOf(50.00));

        // When & Then
        given()
            .when()
            .get("/deals/statut/PUBLIE")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(2)))
            .body("[0].statut", equalTo("PUBLIE"));
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/deals/createur/{uuid} - Devrait récupérer deals d'un créateur")
    void lireParCreateur_DevraitRetournerDealsVendeur() {
        // Given
        creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);
        creerDealAvecImages(vendeur, categorieMaison, 2, BigDecimal.valueOf(50.00));

        // When & Then
        given()
            .when()
            .get("/deals/createur/" + vendeur.getUuid())
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(2)));
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/deals/categorie/{uuid} - Devrait récupérer deals d'une catégorie")
    void lireParCategorie_DevraitRetournerDealsCategorie() {
        // Given
        creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);
        creerDealAvecImages(vendeur, categorieElectronique, 2, BigDecimal.valueOf(75.00));

        // When & Then
        given()
            .when()
            .get("/deals/categorie/" + categorieElectronique.getUuid())
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(2)));
    }

    @Test
    @Order(7)
    @DisplayName("PUT /api/deals/{uuid} - Devrait mettre à jour un deal")
    void mettreAJour_DevraitModifierDeal() {
        // Given
        DealJpa deal = creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("titre", "Deal Modifié");
        updatePayload.put("description", "Description modifiée");
        updatePayload.put("prixDeal", BigDecimal.valueOf(350.00));
        updatePayload.put("prixPart", BigDecimal.valueOf(120.00));
        updatePayload.put("nbParticipants", NB_PARTICIPANTS);
        updatePayload.put("dateDebut", LocalDateTime.now().format(DATE_FORMATTER));
        updatePayload.put("dateFin", LocalDateTime.now().plusDays(45).format(DATE_FORMATTER));
        updatePayload.put("ville", "Quebec");
        updatePayload.put("pays", "Canada");
        updatePayload.put("createurUuid", vendeur.getUuid().toString());
        updatePayload.put("categorieUuid", categorieElectronique.getUuid().toString());

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(updatePayload)
            .when()
            .put("/deals/" + deal.getUuid())
            .then()
            .statusCode(200)
            .body("titre", equalTo("Deal Modifié"))
            .body("ville", equalTo("Quebec"));
    }

    @Test
    @Order(8)
    @DisplayName("PATCH /api/deals/{uuid}/statut - Devrait mettre à jour le statut")
    void mettreAJourStatut_DevraitChangerStatut() {
        // Given
        DealJpa deal = creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);

        Map<String, Object> statutPayload = Map.of("statut", "EXPIRE");

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(statutPayload)
            .when()
            .patch("/deals/" + deal.getUuid() + "/statut")
            .then()
            .statusCode(200)
            .body("statut", equalTo("EXPIRE"));
    }

    @Test
    @Order(9)
    @DisplayName("PATCH /api/deals/{uuid}/images - Devrait mettre à jour les images")
    void mettreAJourImages_DevraitRemplacerImages() {
        // Given
        mockMinioPresignedUrl();
        mockMinioUpload();
        
        DealJpa deal = creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);

        Map<String, Object> imagesPayload = Map.of(
            "listeImages", List.of(
                Map.of("urlImage", "nouvelle_image_1.jpg", "isPrincipal", true),
                Map.of("urlImage", "nouvelle_image_2.jpg", "isPrincipal", false),
                Map.of("urlImage", "nouvelle_image_3.jpg", "isPrincipal", false)
            )
        );

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(imagesPayload)
            .when()
            .patch("/deals/" + deal.getUuid() + "/images")
            .then()
            .statusCode(200)
            .body("listeImages", hasSize(3));
    }

    @Test
    @Order(10)
    @DisplayName("PATCH /api/deals/{uuid}/images/{imageUuid}/confirm - Devrait confirmer upload image")
    void confirmerUploadImage_DevraitMettreStatutUploaded() {
        // Given
        DealJpa deal = creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);
        UUID imageUuid = deal.getImageDealJpas().get(0).getUuid();

        // When & Then
        given()
            .when()
            .patch("/deals/" + deal.getUuid() + "/images/" + imageUuid + "/confirm")
            .then()
            .statusCode(200);
    }

    @Test
    @Order(11)
    @DisplayName("GET /api/deals/{uuid}/images/{imageUuid}/url - Devrait retourner URL image")
    void obtenirUrlImage_DevraitRetournerPresignedUrl() {
        // Given
        mockMinioPresignedUrl();
        DealJpa deal = creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);
        UUID imageUuid = deal.getImageDealJpas().get(0).getUuid();

        // When & Then
        given()
            .when()
            .get("/deals/" + deal.getUuid() + "/images/" + imageUuid + "/url")
            .then()
            .statusCode(200)
            .body("url", notNullValue());
    }

    @Test
    @Order(12)
    @DisplayName("DELETE /api/deals/{uuid} - Devrait supprimer un deal")
    void supprimer_DevraitSupprimerDeal() {
        // Given
        DealJpa deal = creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);

        // When & Then
        given()
            .when()
            .delete("/deals/" + deal.getUuid())
            .then()
            .statusCode(anyOf(is(200), is(204)));

        // Vérifier que le deal n'existe plus
        assertFalse(dealRepository.findById(deal.getUuid()).isPresent());
    }
}

