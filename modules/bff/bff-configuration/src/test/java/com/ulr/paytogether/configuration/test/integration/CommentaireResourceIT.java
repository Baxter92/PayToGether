package com.ulr.paytogether.configuration.test.integration;

import com.ulr.paytogether.configuration.test.AbstractIT;
import com.ulr.paytogether.provider.adapter.entity.CommentaireJpa;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration REST pour CommentaireResource
 * Teste tous les endpoints de gestion des commentaires
 */
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests d'intégration REST : CommentaireResource")
class CommentaireResourceIT extends AbstractIT {

    private DealJpa dealTest;

    @BeforeEach
    void setUpDeal() {
        // Créer un deal pour les tests de commentaires
        dealTest = creerDealAvecImages(vendeur, categorieElectronique, 3, BigDecimal.valueOf(100.00));
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/commentaires - Devrait créer un commentaire")
    void creerCommentaire_DevraitCreerAvecSucces() {
        // Given
        Map<String, Object> commentairePayload = new HashMap<>();
        commentairePayload.put("contenu", "Excellent deal ! Je recommande.");
        commentairePayload.put("note", 5);
        commentairePayload.put("dealUuid", dealTest.getUuid().toString());
        commentairePayload.put("auteurUuid", acheteur1.getUuid().toString());

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(commentairePayload)
            .when()
            .post("/commentaires")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .body("uuid", notNullValue())
            .body("contenu", equalTo("Excellent deal ! Je recommande."))
            .body("note", equalTo(5));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/commentaires/{uuid} - Devrait récupérer un commentaire par UUID")
    void lireParUuid_DevraitRetournerCommentaire() {
        // Given
        CommentaireJpa commentaire = creerCommentaireTest();

        // When & Then
        given()
            .when()
            .get("/commentaires/" + commentaire.getUuid())
            .then()
            .statusCode(200)
            .body("uuid", equalTo(commentaire.getUuid().toString()))
            .body("contenu", notNullValue());
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/commentaires/{uuid} - Devrait retourner 404 si inexistant")
    void lireParUuid_DevraitRetourner404SiInexistant() {
        // Given
        UUID uuidInexistant = UUID.randomUUID();

        // When & Then
        given()
            .when()
            .get("/commentaires/" + uuidInexistant)
            .then()
            .statusCode(404);
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/commentaires/deal/{dealUuid} - Devrait récupérer commentaires d'un deal")
    void lireParDeal_DevraitRetournerCommentairesDuDeal() {
        // Given
        creerCommentaireTest();
        creerCommentaireTest();

        // When & Then
        given()
            .when()
            .get("/commentaires/deal/" + dealTest.getUuid())
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(2)));
    }

    @Test
    @Order(5)
    @DisplayName("PUT /api/commentaires/{uuid} - Devrait mettre à jour un commentaire")
    void mettreAJour_DevraitModifierCommentaire() {
        // Given
        CommentaireJpa commentaire = creerCommentaireTest();

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("contenu", "Commentaire modifié après réflexion.");
        updatePayload.put("note", 4);
        updatePayload.put("dealUuid", dealTest.getUuid().toString());
        updatePayload.put("auteurUuid", acheteur1.getUuid().toString());

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(updatePayload)
            .when()
            .put("/commentaires/" + commentaire.getUuid())
            .then()
            .statusCode(200)
            .body("contenu", equalTo("Commentaire modifié après réflexion."))
            .body("note", equalTo(4));
    }

    @Test
    @Order(6)
    @DisplayName("DELETE /api/commentaires/{uuid} - Devrait supprimer un commentaire")
    void supprimer_DevraitSupprimerCommentaire() {
        // Given
        CommentaireJpa commentaire = creerCommentaireTest();

        // When & Then
        given()
            .when()
            .delete("/commentaires/" + commentaire.getUuid())
            .then()
            .statusCode(anyOf(is(200), is(204)));

        // Vérifier que le commentaire n'existe plus
        assertFalse(commentaireRepository.findById(commentaire.getUuid()).isPresent());
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/commentaires/utilisateur/{auteurUuid} - Devrait récupérer commentaires d'un utilisateur")
    void lireParAuteur_DevraitRetournerCommentairesUtilisateur() {
        // Given
        creerCommentaireTest();
        creerCommentaireTest();

        // When & Then
        given()
            .when()
            .get("/commentaires/utilisateur/" + acheteur1.getUuid())
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(2)));
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/commentaires - Devrait retourner 400 si contenu vide")
    void creerCommentaire_DevraitRetourner400SiContenuVide() {
        // Given
        Map<String, Object> commentairePayload = new HashMap<>();
        commentairePayload.put("contenu", ""); // Contenu vide
        commentairePayload.put("note", 5);
        commentairePayload.put("dealUuid", dealTest.getUuid().toString());
        commentairePayload.put("auteurUuid", acheteur1.getUuid().toString());

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(commentairePayload)
            .when()
            .post("/commentaires")
            .then()
            .statusCode(anyOf(is(400), is(500))); // Validation ou erreur métier
    }

    /**
     * Méthode helper pour créer un commentaire de test
     */
    private CommentaireJpa creerCommentaireTest() {
        CommentaireJpa commentaire = new CommentaireJpa();
        commentaire.setContenu("Super deal, très satisfait !");
        commentaire.setNote(5);
        commentaire.setDealJpa(dealTest);
        commentaire.setUtilisateurJpa(acheteur1);
        return commentaireRepository.save(commentaire);
    }
}

