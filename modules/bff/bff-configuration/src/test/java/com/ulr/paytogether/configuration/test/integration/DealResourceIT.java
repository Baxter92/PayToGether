package com.ulr.paytogether.configuration.test.integration;

import com.ulr.paytogether.configuration.test.AbstractIT;
import com.ulr.paytogether.core.enumeration.StatutCommandeUtilisateur;
import com.ulr.paytogether.provider.adapter.entity.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(DealResourceIT.class);

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
        int nombreImagesInitiales = deal.getImageDealJpas().size();
        log.info("Nombre d'images initiales: {}", nombreImagesInitiales);

        // Payload avec UNIQUEMENT les nouvelles images (les anciennes seront supprimées)
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
            .body("listeImages", hasSize(3))
            .body("listeImages[0].presignUrl", notNullValue())
            .body("listeImages[1].presignUrl", notNullValue())
            .body("listeImages[2].presignUrl", notNullValue())
            .body("listeImages[0].statut", equalTo("PENDING"))
            .body("listeImages[0].isPrincipal", equalTo(true));

        // Vérifier en base que les anciennes images ont été supprimées
        DealJpa dealMisAJour = dealRepository.findById(deal.getUuid()).orElseThrow();
        assertEquals(3, dealMisAJour.getImageDealJpas().size(), 
            "Le deal devrait avoir 3 nouvelles images");
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

    @Test
    @Order(13)
    @DisplayName("DELETE /api/deals/{uuid} - Devrait supprimer un deal avec paiement, adresse et commande en cascade")
    void supprimer_DevraitSupprimerDealAvecConstraintes() {
        // Given - Créer un deal complet avec commande, paiements et adresses
        DealJpa deal = creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);
        log.info("✅ Deal créé: {}", deal.getUuid());

        // Créer une commande pour ce deal
        CommandeJpa commande = creerCommande(deal, vendeur);
        log.info("✅ Commande créée: {}", commande.getNumeroCommande());

        // Créer des paiements avec adresses pour plusieurs acheteurs
        AdresseJpa adresse1 = new AdresseJpa();
        adresse1.setRue("123 Test Street");
        adresse1.setVille("Montreal");
        adresse1.setProvince("QC");
        adresse1.setCodePostal("H1A 1A1");
        adresse1.setPays("Canada");
        adresse1.setHomeDelivery(true);

        AdresseJpa adresse2 = new AdresseJpa();
        adresse2.setRue("456 Avenue Test");
        adresse2.setVille("Quebec");
        adresse2.setProvince("QC");
        adresse2.setCodePostal("G1R 2B2");
        adresse2.setPays("Canada");
        adresse2.setHomeDelivery(false);

        PaiementJpa paiement1 = creerPaiement(commande, acheteur1, adresse1, 1, PRIX_PART);
        PaiementJpa paiement2 = creerPaiement(commande, acheteur2, adresse2, 1, PRIX_PART);
        log.info("✅ Paiements créés: {} et {}", paiement1.getUuid(), paiement2.getUuid());

        // Créer les relations CommandeUtilisateur
        CommandeUtilisateurJpa cu1 = creerCommandeUtilisateur(commande, acheteur1, StatutCommandeUtilisateur.EN_ATTENTE);
        CommandeUtilisateurJpa cu2 = creerCommandeUtilisateur(commande, acheteur2, StatutCommandeUtilisateur.EN_ATTENTE);
        log.info("✅ CommandeUtilisateurs créés: {} et {}", cu1.getUuid(), cu2.getUuid());

        // Vérifier que tout existe avant suppression
        assertTrue(dealRepository.findById(deal.getUuid()).isPresent(), "Le deal devrait exister");
        assertTrue(commandeRepository.findById(commande.getUuid()).isPresent(), "La commande devrait exister");
        assertEquals(2, paiementRepository.findByCommandeJpa(commande).size(), "2 paiements devraient exister");
        assertEquals(2, commandeUtilisateurRepository.findByCommandeJpaUuid(commande.getUuid()).size(), "2 CommandeUtilisateurs devraient exister");

        // Récupérer les UUIDs des entités pour vérifier leur suppression
        UUID dealUuid = deal.getUuid();
        UUID commandeUuid = commande.getUuid();
        UUID paiement1Uuid = paiement1.getUuid();
        UUID paiement2Uuid = paiement2.getUuid();

        // When - Supprimer le deal
        given()
            .when()
            .delete("/deals/" + dealUuid)
            .then()
            .statusCode(anyOf(is(200), is(204)));

        // Then - Vérifier que tout a été supprimé en cascade
        assertFalse(dealRepository.findById(dealUuid).isPresent(), 
            "Le deal devrait être supprimé");
        
        assertFalse(commandeRepository.findById(commandeUuid).isPresent(), 
            "La commande devrait être supprimée en cascade");
        
        assertFalse(paiementRepository.findById(paiement1Uuid).isPresent(), 
            "Le paiement 1 devrait être supprimé en cascade");
        
        assertFalse(paiementRepository.findById(paiement2Uuid).isPresent(), 
            "Le paiement 2 devrait être supprimé en cascade");
        
        assertEquals(0, commandeUtilisateurRepository.findByCommandeJpaUuid(commandeUuid).size(), 
            "Les CommandeUtilisateurs devraient être supprimés en cascade");
        
        // Vérifier que les adresses sont également supprimées (cascade via paiement)
        List<AdresseJpa> adresses = adresseRepository.findAll();
        boolean adresse1Supprimee = adresses.stream()
            .noneMatch(a -> "123 Test Street".equals(a.getRue()) && "Montreal".equals(a.getVille()));
        boolean adresse2Supprimee = adresses.stream()
            .noneMatch(a -> "456 Avenue Test".equals(a.getRue()) && "Quebec".equals(a.getVille()));
        
        assertTrue(adresse1Supprimee, "L'adresse 1 devrait être supprimée en cascade");
        assertTrue(adresse2Supprimee, "L'adresse 2 devrait être supprimée en cascade");

        log.info("✅ Suppression en cascade validée : Deal + Commande + Paiements + Adresses + CommandeUtilisateurs");
    }
}

