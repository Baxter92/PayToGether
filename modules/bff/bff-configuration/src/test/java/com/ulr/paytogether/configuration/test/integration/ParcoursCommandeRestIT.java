package com.ulr.paytogether.configuration.test.integration;

import com.ulr.paytogether.configuration.test.AbstractIT;
import com.ulr.paytogether.core.enumeration.StatutCommande;
import com.ulr.paytogether.core.enumeration.StatutCommandeUtilisateur;
import com.ulr.paytogether.provider.adapter.entity.CommandeJpa;
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
 * Tests d'intégration avec appels REST réels
 * Teste le parcours complet via les endpoints API
 * 
 * ✅ Sécurité désactivée via TestSecurityConfig pour faciliter les tests
 * ✅ Pas besoin de token JWT en tests
 */
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests d'intégration REST : Parcours Commande Complet")
class ParcoursCommandeRestIT extends AbstractIT {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final BigDecimal PRIX_PART = BigDecimal.valueOf(100.00);
    private static final int NB_PARTICIPANTS = 3;

    @Test
    @Order(1)
    @DisplayName("REST : POST /api/deals - Créer un deal")
    void test01_creerDealViaRest() {
        // Given : Mock MinIO
        mockMinioPresignedUrl();
        mockMinioUpload();

        // Préparer le payload
        Map<String, Object> dealPayload = new HashMap<>();
        dealPayload.put("titre", "Deal Test REST");
        dealPayload.put("description", "Description du deal via REST");
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

        // Images
        List<Map<String, Object>> images = List.of(
                Map.of("urlImage", "test_image_1.jpg", "isPrincipal", true),
                Map.of("urlImage", "test_image_2.jpg", "isPrincipal", false)
        );
        dealPayload.put("listeImages", images);

        // When : Appeler l'API REST
        String dealUuid = given()
                .contentType(ContentType.JSON)
                .body(dealPayload)
                .when()
                .post("/deals")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("uuid", notNullValue())
                .body("titre", equalTo("Deal Test REST"))
                .body("statut", equalTo("PUBLIE"))
                .body("nbParticipants", equalTo(NB_PARTICIPANTS))
                .extract()
                .path("uuid");

        // Then : Vérifier en base de données
        DealJpa dealCreated = dealRepository.findById(java.util.UUID.fromString(dealUuid)).orElseThrow();
        assertNotNull(dealCreated);
        assertEquals("Deal Test REST", dealCreated.getTitre());

        System.out.println("✅ REST : Deal créé via API avec UUID " + dealUuid);
    }

    @Test
    @Order(2)
    @DisplayName("REST : POST /api/square-payments - Initier un paiement Square")
    void test02_creerPaiementSquareViaRest() {
        // Given : Deal créé
        dealTest = creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);
        commandeTest = creerCommande(dealTest, acheteur1);
        mockSquarePaymentSuccess();

        Map<String, Object> adressePayload = new HashMap<>();
        adressePayload.put("rue", "123 Test Street");
        adressePayload.put("ville", "Montreal");
        adressePayload.put("codePostal", "H1A 1A1");
        adressePayload.put("numeroPhone", "+155687888");

        // Préparer le payload du paiement
        Map<String, Object> paiementPayload = new HashMap<>();
        paiementPayload.put("dealUuid", dealTest.getUuid().toString());
        paiementPayload.put("utilisateurUuid", acheteur1.getUuid().toString());
        paiementPayload.put("nombreDePart", 1);
        paiementPayload.put("montant", 125);
        paiementPayload.put("methodePaiement", "SQUARE_CARD");
        paiementPayload.put("squareToken", "cnon:test_square_token_123");
        paiementPayload.put("adresse", adressePayload);

        // When : Appeler l'API REST Square Payment
        given()
                .contentType(ContentType.JSON)
                .body(paiementPayload)
                .when()
                .post("/square-payments")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("uuid", notNullValue())
                .body("statut", notNullValue());

        System.out.println("✅ REST : Paiement Square créé via API");
    }

    @Test
    @Order(3)
    @DisplayName("REST : POST /api/admin/commandes/{uuid}/payout/valider - Valider payout")
    void test03_validerPayoutViaRest() {
        // Given : Commande complétée
        dealTest = creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);
        commandeTest = creerCommande(dealTest, acheteur1);
        simulerPaiementsComplets(commandeTest, List.of(acheteur1, acheteur2, acheteur3));

        CommandeJpa commandeReload = commandeRepository.findById(commandeTest.getUuid()).orElseThrow();
        assertEquals(StatutCommande.COMPLETEE, commandeReload.getStatut());

        // Préparer le payload
        Map<String, Object> payoutPayload = new HashMap<>();
        payoutPayload.put("dateDepotPayout", LocalDateTime.now().format(DATE_FORMATTER));

        // When : Appeler l'API REST pour valider le payout
        given()
                .contentType(ContentType.JSON)
                .body(payoutPayload)
                .when()
                .post("/admin/commandes/" + commandeReload.getUuid() + "/payout/valider")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("uuid", equalTo(commandeReload.getUuid().toString()));

        // Then : Vérifier en base de données
        CommandeJpa commandeReload2 = commandeRepository.findById(commandeTest.getUuid()).orElseThrow();
        assertEquals(StatutCommande.PAYOUT, commandeReload2.getStatut(), "Le statut devrait être PAYOUT");
        
        System.out.println("✅ REST : Payout validé avec statut " + commandeReload2.getStatut());
    }

    @Test
    @Order(4)
    @DisplayName("REST : POST /api/admin/commandes/{uuid}/facture/upload - Upload facture vendeur")
    void test04_uploadFactureVendeurViaRest() {
        // Given : Commande en PAYOUT
        dealTest = creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);
        commandeTest = creerCommande(dealTest, acheteur1);
        simulerPaiementsComplets(commandeTest, List.of(acheteur1, acheteur2, acheteur3));
        simulerDepotPayout(commandeTest);

        CommandeJpa commandeReload = commandeRepository.findById(commandeTest.getUuid()).orElseThrow();
        assertEquals(StatutCommande.PAYOUT, commandeReload.getStatut());

        // When : Appeler l'API REST pour uploader la facture
        given()
                .multiPart("facture", "test_facture.pdf", "PDF content".getBytes())
                .when()
                .post("/admin/commandes/" + commandeReload.getUuid() + "/facture/upload")
                .then()
                .statusCode(anyOf(is(200), is(201)));

        System.out.println("✅ REST : Facture uploadée avec succès");
    }

    @Test
    @Order(5)
    @DisplayName("REST : POST /api/admin/commandes/{uuid}/factures/valider - Valider factures clients")
    void test05_validerFacturesClientsViaRest() {
        // Given : Commande en INVOICE_CUSTOMER
        dealTest = creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);
        commandeTest = creerCommande(dealTest, acheteur1);
        simulerPaiementsComplets(commandeTest, List.of(acheteur1, acheteur2, acheteur3));
        simulerDepotPayout(commandeTest);
        simulerUploadFactureMarchand(commandeTest);
        simulerTraitementFactureVendeur(commandeTest);

        CommandeJpa commandeReload = commandeRepository.findById(commandeTest.getUuid()).orElseThrow();
        assertEquals(StatutCommande.INVOICE_CUSTOMER, commandeReload.getStatut());

        // Récupérer les UUIDs des CommandeUtilisateurs
        List<UUID> utilisateursUuids = commandeUtilisateurRepository.findByCommandeJpaUuid(commandeReload.getUuid()).stream()
                .map(cu -> cu.getUtilisateurJpa().getUuid())
                .toList();

        // Préparer le payload de validation
        Map<String, Object> validationPayload = new HashMap<>();
        validationPayload.put("utilisateurUuids", utilisateursUuids);

        // When : Appeler l'API REST pour valider les factures
        given()
                .contentType(ContentType.JSON)
                .body(validationPayload)
                .when()
                .post("/admin/commandes/" + commandeReload.getUuid() + "/factures/valider")
                .then()
                .statusCode(anyOf(is(200), is(201)));

        System.out.println("✅ REST : Factures clients validées avec succès");
    }

    @Test
    @Order(6)
    @DisplayName("REST : GET /api/admin/commandes - Lister toutes les commandes")
    void test06_listerCommandesViaRest() {
        // Given : Plusieurs commandes créées
        dealTest = creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);
        commandeTest = creerCommande(dealTest, acheteur1);

        DealJpa deal2 = creerDealAvecImages(vendeur, categorieMaison, 2, BigDecimal.valueOf(50.00));
        creerCommande(deal2, acheteur2);

        // When : Appeler l'API REST pour lister les commandes
        given()
                .when()
                .get("/admin/commandes")
                .then()
                .statusCode(is(200))
                .body("$", notNullValue());

        System.out.println("✅ REST : Liste des commandes récupérée avec succès");
    }

    @Test
    @Order(7)
    @DisplayName("REST : GET /api/admin/commandes/marchand/{uuid} - Lister commandes d'un marchand")
    void test07_listerCommandesMarchandViaRest() {
        // Given : Commandes du vendeur
        dealTest = creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);
        commandeTest = creerCommande(dealTest, acheteur1);

        // When : Appeler l'API REST
        given()
                .when()
                .get("/admin/commandes/marchand/" + vendeur.getUuid())
                .then()
                .statusCode(is(200))
                .body("$", notNullValue());

        System.out.println("✅ REST : Commandes du marchand récupérées avec succès");
    }

    @Test
    @Order(8)
    @DisplayName("REST : GET /api/admin/commandes/{uuid} - Récupérer une commande par UUID")
    void test08_recupererCommandeParUuidViaRest() {
        // Given : Commande créée
        dealTest = creerDealAvecImages(vendeur, categorieElectronique, NB_PARTICIPANTS, PRIX_PART);
        commandeTest = creerCommande(dealTest, acheteur1);

        // When : Appeler l'API REST
        given()
                .when()
                .get("/admin/commandes/" + commandeTest.getUuid())
                .then()
                .statusCode(is(200))
                .body("uuid", equalTo(commandeTest.getUuid().toString()));

        System.out.println("✅ REST : Commande récupérée avec succès");
    }

    @Test
    @Order(9)
    @DisplayName("✅ Parcours REST complet : Création → Paiements → Validation → Terminée")
    void parcoursRestComplet_dealVersCommandeTerminee() {
        System.out.println("\n🚀 DÉBUT DU PARCOURS REST COMPLET\n");

        // ===================================================================
        // ÉTAPE 1 : Créer un deal via POST /api/deals
        // ===================================================================
        mockMinioPresignedUrl();
        mockMinioUpload();

        Map<String, Object> dealPayload = new HashMap<>();
        dealPayload.put("titre", "Deal Parcours Complet");
        dealPayload.put("description", "Test du parcours complet de création à commande terminée");
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
        dealPayload.put("listePointsForts", List.of("Qualité premium", "Livraison gratuite"));
        dealPayload.put("listeImages", List.of(
                Map.of("urlImage", "test_image_principale.jpg", "isPrincipal", true),
                Map.of("urlImage", "test_image_secondaire.jpg", "isPrincipal", false)
        ));

        String dealUuid = given()
                .contentType(ContentType.JSON)
                .body(dealPayload)
                .when()
                .post("/deals")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("uuid", notNullValue())
                .body("titre", equalTo("Deal Parcours Complet"))
                .extract()
                .path("uuid");

        System.out.println("✅ ÉTAPE 1 : Deal créé avec UUID " + dealUuid);

        // ===================================================================
        // ÉTAPE 2 : Créer 3 paiements Square (3 acheteurs)
        // ===================================================================
        mockSquarePaymentSuccess();

        List<String> acheteursUuids = List.of(
                acheteur1.getUuid().toString(),
                acheteur2.getUuid().toString(),
                acheteur3.getUuid().toString()
        );

        for (int i = 0; i < acheteursUuids.size(); i++) {
            Map<String, Object> adressePayload = new HashMap<>();
            adressePayload.put("rue", "123 Test Street"+ (i + 1));
            adressePayload.put("ville", "Montreal");
            adressePayload.put("codePostal", "H1A 1A1"+ (i + 1));
            adressePayload.put("numeroPhone", "+155687888");

            Map<String, Object> paiementPayload = new HashMap<>();
            paiementPayload.put("dealUuid", dealUuid);
            paiementPayload.put("utilisateurUuid", acheteursUuids.get(i));
            paiementPayload.put("nombreDePart", 1);
            paiementPayload.put("montant", 100);
            paiementPayload.put("methodePaiement", "SQUARE_CARD");
            paiementPayload.put("adresse", adressePayload);
            paiementPayload.put("squareToken", "cnon:test_token_acheteur_" + (i + 1));

            given()
                    .contentType(ContentType.JSON)
                    .body(paiementPayload)
                    .when()
                    .post("/square-payments")
                    .then()
                    .statusCode(anyOf(is(200), is(201)))
                    .body("uuid", notNullValue());

            System.out.println("✅ ÉTAPE 2." + (i + 1) + " : Paiement acheteur " + (i + 1) + " créé");
        }

        // ===================================================================
        // ÉTAPE 3 : Récupérer la commande créée automatiquement
        // ===================================================================
        DealJpa dealCree = dealRepository.findById(java.util.UUID.fromString(dealUuid)).orElseThrow();
        CommandeJpa commandeCree = commandeRepository.findByDealJpa(dealCree)
                .orElseThrow(() -> new AssertionError("Une commande doit être créée automatiquement"));

        System.out.println("✅ ÉTAPE 3 : Commande récupérée avec UUID " + commandeCree.getUuid());

        //event record set statut commplete
        setStatut(commandeCree, StatutCommande.COMPLETEE);
        CommandeJpa commandeReload = commandeRepository.findById(commandeCree.getUuid())
                .orElseThrow(() -> new AssertionError("Une commande doit être créée automatiquement"));
        assertEquals(StatutCommande.COMPLETEE, commandeReload.getStatut(), "La commande devrait être COMPLETEE après 3 paiements");

        // ===================================================================
        // ÉTAPE 4 : Valider le payout (COMPLETEE → PAYOUT)
        // ===================================================================
        Map<String, Object> payoutPayload = new HashMap<>();
        payoutPayload.put("dateDepotPayout", LocalDateTime.now()
                .format(DATE_FORMATTER));

        given()
                .contentType(ContentType.JSON)
                .body(payoutPayload)
                .when()
                .post("/admin/commandes/" + commandeCree.getUuid() + "/payout/valider")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("uuid", equalTo(commandeCree.getUuid().toString()));

        // Recharger la commande depuis la BDD
        CommandeJpa commandeApresPayoutReload = commandeRepository.findById(commandeReload.getUuid()).orElseThrow();
        System.out.println("✅ ÉTAPE 4 : Payout validé - Statut: " + commandeApresPayoutReload.getStatut());
        assertEquals(StatutCommande.PAYOUT, commandeApresPayoutReload.getStatut());

        // ===================================================================
        // ÉTAPE 5 : Upload de la facture vendeur (PAYOUT → INVOICE_CUSTOMER)
        // ===================================================================
        given()
                .multiPart("facture", "facture_vendeur.pdf", "Contenu PDF de test".getBytes())
                .when()
                .post("/admin/commandes/" + commandeReload.getUuid() + "/facture/upload")
                .then()
                .statusCode(anyOf(is(200), is(201)));

        CommandeJpa commandeApresFacture = commandeRepository.findById(commandeReload.getUuid()).orElseThrow();
        System.out.println("✅ ÉTAPE 5 : Facture uploadée - Statut: " + commandeApresFacture.getStatut());
        assertEquals(StatutCommande.INVOICE_SELLER, commandeApresFacture.getStatut());

        // ===================================================================
        // ÉTAPE 6 : Valider les factures clients (INVOICE_CUSTOMER → TERMINEE)
        // ===================================================================

        // Simuler le traitement de la facture vendeur via le handler SellerInvoiceUploadedHandler
        // Cela génère et envoie les factures clients puis met à jour le statut à INVOICE_CUSTOMER
        simulerTraitementFactureVendeur(commandeApresFacture);
        CommandeJpa commandeApresMailClient = commandeRepository.findById(commandeApresFacture.getUuid()).orElseThrow();

        List<String> utilisateursValides = commandeApresMailClient.getUtilisateursCommande().stream()
                .map(cu -> cu.getUtilisateurJpa().getUuid().toString())
                .toList();

        Map<String, Object> validationPayload = new HashMap<>();
        validationPayload.put("utilisateurUuids", utilisateursValides);

        given()
                .contentType(ContentType.JSON)
                .body(validationPayload)
                .when()
                .post("/admin/commandes/" + commandeApresMailClient.getUuid() + "/factures/valider")
                .then()
                .statusCode(anyOf(is(200), is(201)));

        CommandeJpa commandeFinale = commandeRepository.findById(commandeApresMailClient.getUuid()).orElseThrow();
        System.out.println("✅ ÉTAPE 6 : Factures validées - Statut: " + commandeFinale.getStatut());
        assertEquals(StatutCommande.TERMINEE, commandeFinale.getStatut());

        // ===================================================================
        // VÉRIFICATIONS FINALES
        // ===================================================================
        System.out.println("\n🎉 PARCOURS COMPLET TERMINÉ AVEC SUCCÈS !\n");
        System.out.println("📊 Récapitulatif :");
        System.out.println("   - Deal UUID: " + dealUuid);
        System.out.println("   - Commande UUID: " + commandeFinale.getUuid());
        System.out.println("   - Statut final: " + commandeFinale.getStatut());
        System.out.println("   - Nombre de participants: " + commandeFinale.getUtilisateursCommande().size());
        System.out.println("   - Montant total: " + commandeFinale.getMontantTotal());

        // Assertions finales
        assertEquals(3, commandeFinale.getUtilisateursCommande().size());
        assertTrue(commandeFinale.getUtilisateursCommande().stream()
                .allMatch(cu -> cu.getStatutCommandeUtilisateur() == StatutCommandeUtilisateur.VALIDEE));
    }
}

