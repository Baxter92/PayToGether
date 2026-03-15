package com.ulr.paytogether.configuration.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.ulr.paytogether.bff.event.handler.impl.SquarePaymentHandler;
import com.ulr.paytogether.configuration.test.config.TestConfig;
import com.ulr.paytogether.configuration.test.config.TestSecurityConfig;
import com.ulr.paytogether.core.enumeration.*;
import com.ulr.paytogether.provider.adapter.entity.*;
import com.ulr.paytogether.provider.repository.*;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Classe de base pour les tests d'intégration
 * Contient tous les fixtures JPA et la configuration WireMock
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestConfig.class, TestSecurityConfig.class})
//@Testcontainers  // Commenté car on utilise H2 pour les tests
public abstract class AbstractIT {

    private static final Logger log = LoggerFactory.getLogger(AbstractIT.class);

    @LocalServerPort
    protected int port;

    protected static WireMockServer wireMockServer;

    // Repositories injectés pour créer les fixtures
    @Autowired
    protected CategorieRepository categorieRepository;

    @Autowired
    protected UtilisateurRepository utilisateurRepository;

    @Autowired
    protected DealRepository dealRepository;

    @Autowired
    protected CommandeRepository commandeRepository;

    @Autowired
    protected PaiementRepository paiementRepository;

    @Autowired
    protected CommandeUtilisateurRepository commandeUtilisateurRepository;

    @Autowired
    protected AdresseRepository adresseRepository;

    @Autowired
    protected ValidationTokenRepository validationTokenRepository;

    @Autowired
    protected PubliciteRepository publiciteRepository;

    @Autowired
    protected CommentaireRepository commentaireRepository;

    // Fixtures JPA (accessibles aux tests)
    protected CategorieJpa categorieElectronique;
    protected CategorieJpa categorieMaison;
    protected UtilisateurJpa vendeur;
    protected UtilisateurJpa acheteur1;
    protected UtilisateurJpa acheteur2;
    protected UtilisateurJpa acheteur3;
    protected DealJpa dealTest;
    protected CommandeJpa commandeTest;
    protected AdresseJpa adresseTest;
    protected AdresseJpa adresseTest2;
    protected AdresseJpa adresseTest3;

    /**
     * Configuration Testcontainers PostgreSQL (optionnel, H2 par défaut)
     * Décommentez si vous voulez utiliser PostgreSQL en tests
     */
    // @Container
    // protected static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")
    //         .withDatabaseName("testdb")
    //         .withUsername("test")
    //         .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Configuration WireMock
        registry.add("wiremock.server.port", () -> wireMockServer.port());
        
        // Configuration PostgreSQL Testcontainers (si activé)
        // registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        // registry.add("spring.datasource.username", postgresContainer::getUsername);
        // registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .dynamicPort()
                .usingFilesUnderClasspath("wiremock"));
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setupRestAssured() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        creerCategories();
        creerUtilisateurs();
    }

    @BeforeEach
    @Transactional
    void setupFixtures() {
        // Créer les fixtures de base

        // Note : adresses créées automatiquement avec les paiements
    }

    @AfterEach
    void tearDown() {
        wireMockServer.resetAll();
        nettoyerBaseDeDonnees();
    }

    /**
     * Nettoie toutes les tables de la base de données
     */
    protected void nettoyerBaseDeDonnees() {
        // Ordre important : supprimer les dépendances avant les entités référencées
        adresseRepository.deleteAll();
        paiementRepository.deleteAll();
        commandeUtilisateurRepository.deleteAll();
        commandeRepository.deleteAll();
        dealRepository.deleteAll();
        utilisateurRepository.deleteAll();
        categorieRepository.deleteAll();
    }

    /**
     * Crée les catégories de test
     */
    protected void creerCategories() {
        categorieElectronique = CategorieJpa.builder()
                .nom("Électronique")
                .description("Produits électroniques")
                .build();
        categorieElectronique = categorieRepository.save(categorieElectronique);

        categorieMaison = CategorieJpa.builder()
                .nom("Maison & Jardin")
                .description("Articles pour la maison")
                .build();
        categorieMaison = categorieRepository.save(categorieMaison);
    }

    /**
     * Crée les utilisateurs de test
     */
    protected void creerUtilisateurs() {
        // Vendeur
        Optional<UtilisateurJpa> vendeur1 = utilisateurRepository.findByEmail(Optional.ofNullable(vendeur).map(UtilisateurJpa::getEmail).orElse(null));
        if (vendeur1.isPresent()) {
            vendeur = vendeur1.get();
        } else {
            vendeur = new UtilisateurJpa();
            vendeur.setNom("Vendeur");
            vendeur.setPrenom("Test");
            vendeur.setEmail("vendeur@test.com");
            vendeur.setMotDePasse("$2a$10$abcdefghijklmnopqrstuvwxyz");
            vendeur.setRole(RoleUtilisateur.VENDEUR);
            vendeur.setStatut(StatutUtilisateur.ACTIF);
            vendeur = utilisateurRepository.save(vendeur);
        }

        // Acheteur 1
        Optional<UtilisateurJpa> acheteur10 = utilisateurRepository.findByEmail(Optional.ofNullable(acheteur1).map(UtilisateurJpa::getEmail).orElse(null));
        if (acheteur10.isPresent()) {
            acheteur1 = acheteur10.get();
        } else {
            acheteur1 = new UtilisateurJpa();
            acheteur1.setNom("Acheteur");
            acheteur1.setPrenom("Un");
            acheteur1.setEmail("acheteur1@test.com");
            acheteur1.setMotDePasse("$2a$10$abcdefghijklmnopqrstuvwxyz");
            acheteur1.setRole(RoleUtilisateur.UTILISATEUR);
            acheteur1.setStatut(StatutUtilisateur.ACTIF);
            acheteur1 = utilisateurRepository.save(acheteur1);
        }



        // Acheteur 2
        Optional<UtilisateurJpa> acheteur20 = utilisateurRepository.findByEmail(Optional.ofNullable(acheteur2).map(UtilisateurJpa::getEmail).orElse(null));
        if (acheteur20.isPresent()) {
            acheteur2 = acheteur20.get();
        } else {
            acheteur2 = new UtilisateurJpa();
            acheteur2.setNom("Acheteur");
            acheteur2.setPrenom("Deux");
            acheteur2.setEmail("acheteur2@test.com");
            acheteur2.setMotDePasse("$2a$10$abcdefghijklmnopqrstuvwxyz");
            acheteur2.setRole(RoleUtilisateur.UTILISATEUR);
            acheteur2.setStatut(StatutUtilisateur.ACTIF);
            acheteur2 = utilisateurRepository.save(acheteur2);
        }

        // Acheteur 3
        Optional<UtilisateurJpa> acheteur30 = utilisateurRepository.findByEmail(Optional.ofNullable(acheteur3).map(UtilisateurJpa::getEmail).orElse(null));
        if (acheteur30.isPresent()) {
            acheteur3 = acheteur30.get();
        } else {
            acheteur3 = new UtilisateurJpa();
            acheteur3.setNom("Acheteur");
            acheteur3.setPrenom("Trois");
            acheteur3.setEmail("acheteur3@test.com");
            acheteur3.setMotDePasse("$2a$10$abcdefghijklmnopqrstuvwxyz");
            acheteur3.setRole(RoleUtilisateur.UTILISATEUR);
            acheteur3.setStatut(StatutUtilisateur.ACTIF);
            acheteur3 = utilisateurRepository.save(acheteur3);
        }

    }

    /**
     * Crée les adresses de test
     */
    protected void creerAdresses() {
        adresseTest = new AdresseJpa();
        adresseTest.setRue("123 Test Street");
        adresseTest.setVille("Montreal");
        adresseTest.setProvince("QC");
        adresseTest.setCodePostal("H1A 1A1");
        adresseTest.setPays("Canada");
        adresseTest.setHomeDelivery(true);
        adresseTest = adresseRepository.save(adresseTest);
    }

    /**
     * Crée un deal de test avec images
     */
    protected DealJpa creerDealAvecImages(UtilisateurJpa createur, CategorieJpa categorie, int nbParticipants, BigDecimal prixPart) {
        DealJpa deal = new DealJpa();
        deal.setTitre("Deal Test Intégration");
        deal.setDescription("Description du deal de test pour les tests d'intégration");
        deal.setPrixDeal(prixPart.multiply(BigDecimal.valueOf(nbParticipants)));
        deal.setPrixPart(prixPart);
        deal.setNbParticipants(nbParticipants);
        deal.setDateDebut(LocalDateTime.now());
        deal.setDateFin(LocalDateTime.now().plusDays(30));
        deal.setStatut(StatutDeal.PUBLIE);
        deal.setVille("Montreal");
        deal.setPays("Canada");
        deal.setMarchandJpa(createur);
        deal.setCategorieJpa(categorie);
        deal.setListePointsForts(List.of("Qualité premium", "Livraison gratuite"));
        deal.setImageDealJpas(new ArrayList<>());

        // Créer l'image principale
        ImageDealJpa image = new ImageDealJpa();
        image.setUrlImage("test_image_" + System.currentTimeMillis() + ".jpg");
        image.setIsPrincipal(true);
        image.setStatut(StatutImage.UPLOADED);
        image.setDealJpa(deal);
        deal.getImageDealJpas().add(image);

        // Ajouter une image secondaire
        ImageDealJpa imageSecondaire = new ImageDealJpa();
        imageSecondaire.setUrlImage("deals/test_image_secondaire_" + System.currentTimeMillis() + ".jpg");
        imageSecondaire.setIsPrincipal(false);
        imageSecondaire.setStatut(StatutImage.UPLOADED);
        imageSecondaire.setDealJpa(deal);
        deal.getImageDealJpas().add(imageSecondaire);

        return dealRepository.save(deal);
    }

    /**
     * Crée une commande avec statut EN_COURS
     */
    protected CommandeJpa creerCommande(DealJpa deal, UtilisateurJpa utilisateur) {
        String numeroCommande = "CMD-" + System.currentTimeMillis();

        CommandeJpa commande = new CommandeJpa();
        commande.setNumeroCommande(numeroCommande);
        commande.setMontantTotal(deal.getPrixDeal());
        commande.setStatut(StatutCommande.EN_COURS);
        commande.setMarchandJpa(utilisateur);
        commande.setDealJpa(deal);
        commande.setDateCommande(LocalDateTime.now());
        commande.setUtilisateursCommande(new ArrayList<>());

        return commandeRepository.save(commande);
    }

    /**
     * Crée un paiement avec statut CONFIRME et son adresse
     */
    protected PaiementJpa creerPaiement(CommandeJpa commande, UtilisateurJpa utilisateur, 
                                        AdresseJpa adresse, int nombreDePart, BigDecimal montantPart) {
        // Calcul selon la formule canadienne
        double montantTransaction = (adresse != null && adresse.isHomeDelivery()) ? 12.0 : 0.0;
        double montantDuPaiement = montantPart.multiply(BigDecimal.valueOf(nombreDePart)).doubleValue();
        double montantTotalFraisService = montantDuPaiement + (0.05 * montantDuPaiement);
        double tva = 0.05 * montantTotalFraisService;
        double montantTotal = montantTotalFraisService + tva + montantTransaction;

        // Créer d'abord le paiement
        PaiementJpa paiement = new PaiementJpa();
        paiement.setMontant(BigDecimal.valueOf(montantTotal));
        paiement.setStatut(StatutPaiement.CONFIRME);
        paiement.setMethodePaiement(MethodePaiement.CARTE_CREDIT);
        paiement.setTransactionId("TEST_TXN_" + System.currentTimeMillis());
        paiement.setSquarePaymentId("test_payment_" + System.currentTimeMillis());
        paiement.setSquareOrderId("test_order_" + System.currentTimeMillis());
        paiement.setSquareLocationId("test_location_123");
        paiement.setSquareReceiptUrl("https://squareup.com/receipt/test");
        paiement.setUtilisateurJpa(utilisateur);
        paiement.setCommandeJpa(commande);
        paiement.setDatePaiement(LocalDateTime.now());

        PaiementJpa paiementSave = paiementRepository.save(paiement);
        
        // Créer l'adresse et la lier au paiement
        if (adresse == null) {
            adresse = new AdresseJpa();
            adresse.setRue("123 Test Street");
            adresse.setVille("Montreal");
            adresse.setProvince("QC");
            adresse.setCodePostal("H1A 1A1");
            adresse.setPays("Canada");
            adresse.setHomeDelivery(true);
        }
        adresse.setPaiement(paiementSave);
        adresseRepository.save(adresse);

        return paiementSave;
    }

    protected CommandeJpa setStatut(CommandeJpa commandeJpa, StatutCommande statut) {
        CommandeJpa commandeJpa1 = commandeRepository.findById(commandeJpa.getUuid()).orElseThrow();
        commandeJpa1.setStatut(statut);
        return commandeRepository.save(commandeJpa1);
    }

    /**
     * Crée une CommandeUtilisateur
     */
    protected CommandeUtilisateurJpa creerCommandeUtilisateur(CommandeJpa commande, 
                                                              UtilisateurJpa utilisateur,
                                                              StatutCommandeUtilisateur statut) {
        CommandeUtilisateurJpa commandeUtilisateur = new CommandeUtilisateurJpa();
        commandeUtilisateur.setCommandeJpa(commande);
        commandeUtilisateur.setUtilisateurJpa(utilisateur);
        commandeUtilisateur.setStatutCommandeUtilisateur(statut);

        commandeUtilisateur = commandeUtilisateurRepository.save(commandeUtilisateur);
        
        // Ajouter à la commande
        if (commande.getUtilisateursCommande() == null) {
            commande.setUtilisateursCommande(new ArrayList<>());
        }
        commande.getUtilisateursCommande().add(commandeUtilisateur);
        
        return commandeUtilisateur;
    }

    /**
     * Mock creation compte
     * @param statut
     * @return
     */
    protected UtilisateurJpa creerUtilisateur(StatutUtilisateur  statut) {
        UtilisateurJpa utilisateur = new UtilisateurJpa();
        utilisateur.setEmail("test_email");
        utilisateur.setNom("test_nom");
        utilisateur.setPrenom("test_prenom");
        utilisateur.setMotDePasse("test_mot_de_passe");
        utilisateur.setRole(RoleUtilisateur.UTILISATEUR);
        utilisateur.setStatut(statut);
        return utilisateurRepository.save(utilisateur);
    }

    /**
     * Mock validation token
     * @param uuid
     * @param token
     * @return
     */
    protected ValidationTokenJpa setValidationToken(UUID uuid, String token) {
        UtilisateurJpa utilisateurJpa = utilisateurRepository.findById(uuid).orElseThrow();
        ValidationTokenJpa validationTokenJpa1 = new ValidationTokenJpa();
        validationTokenJpa1.setToken(token);
        validationTokenJpa1.setTypeToken("VALIDATION_COMPTE");
        validationTokenJpa1.setUtilisateurUuid(utilisateurJpa.getUuid());
        validationTokenJpa1.setDateExpiration(LocalDateTime.now().plusHours(1));
        validationTokenJpa1.setUtilise(false);
        return validationTokenRepository.save(validationTokenJpa1);
    }

    /**
     * Mock WireMock pour Square Payment Success
     */
    protected void mockSquarePaymentSuccess() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/v2/payments"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("square/payment-success.json")));
    }

    /**
     * Mock WireMock pour Square Payment Failed
     */
    protected void mockSquarePaymentFailed() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/v2/payments"))
                .willReturn(WireMock.aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("square/payment-failed.json")));
    }

    /**
     * Mock WireMock pour MinIO presigned URL
     */
    protected void mockMinioPresignedUrl() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/test-bucket/.*\\?.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"url\": \"http://localhost:" + wireMockServer.port() + "/test-presigned-url\"}")));
    }

    /**
     * Mock WireMock pour MinIO upload
     */
    protected void mockMinioUpload() {
        wireMockServer.stubFor(WireMock.put(WireMock.urlMatching("/test-bucket/.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)));
    }

    // =========================================================================
    // MOCKS KEYCLOAK
    // =========================================================================
    
    /**
     * Les mocks Keycloak sont gérés automatiquement par WireMock via les fichiers JSON :
     * - mappings/keycloak-admin-auth.json : Authentification admin
     * - mappings/keycloak-create-user.json : Création utilisateur
     * - mappings/keycloak-get-user.json : Récupération utilisateur
     * - mappings/keycloak-update-user.json : Mise à jour utilisateur
     * - mappings/keycloak-delete-user.json : Suppression utilisateur
     * - mappings/keycloak-get-roles.json : Liste des rôles
     * - mappings/keycloak-assign-role.json : Assigner un rôle
     * - mappings/keycloak-remove-role.json : Retirer un rôle
     * 
     * Les fichiers de réponses sont dans : wiremock/keycloak/*.json
     * 
     * ✅ Aucune méthode mock n'est nécessaire - WireMock charge automatiquement
     * les mappings au démarrage depuis le classpath "wiremock"
     */

    /**
     * Simule le paiement de toutes les parts du deal.
     * Crée les paiements et CommandeUtilisateurs nécessaires.
     * 
     * Note : Dans un environnement réel, c'est le handler PaymentSuccessfulHandler
     * qui mettrait à jour le statut de la commande. Ici, on simule directement
     * le résultat final pour les tests d'intégration.
     */
    protected void simulerPaiementsComplets(CommandeJpa commande, List<UtilisateurJpa> acheteurs) {
        DealJpa deal = commande.getDealJpa();
        int nbPartsParAcheteur = deal.getNbParticipants() / acheteurs.size();
        
        for (UtilisateurJpa acheteur : acheteurs) {
            // Créer le paiement (l'adresse est créée automatiquement)
            PaiementJpa paiement = creerPaiement(commande, acheteur, null, nbPartsParAcheteur, deal.getPrixPart());
            
            // Créer CommandeUtilisateur
            creerCommandeUtilisateur(commande, acheteur, StatutCommandeUtilisateur.EN_ATTENTE);
            
            log.info("✅ Paiement simulé pour l'acheteur {}", acheteur.getEmail());
        }
        
        // Mettre à jour le statut de la commande à COMPLETEE
        // (équivalent au résultat du handler PaymentSuccessfulHandler)
        setStatut(commande, StatutCommande.COMPLETEE);
        log.info("✅ Commande {} marquée comme COMPLETEE", commande.getNumeroCommande());
    }

    /**
     * Simule le traitement de l'upload de facture vendeur.
     * 
     * Note : Dans un environnement réel, c'est le handler SellerInvoiceUploadedHandler
     * qui générerait les factures clients et mettrait à jour le statut. Ici, on simule
     * directement le résultat final pour les tests d'intégration.
     */
    protected void simulerTraitementFactureVendeur(CommandeJpa commande) {
        CommandeJpa commandeJpa = commandeRepository.findById(commande.getUuid()).orElseThrow();
        
        // Simuler le résultat du handler : statut INVOICE_CUSTOMER
        // (équivalent au résultat du handler SellerInvoiceUploadedHandler)
        commandeJpa.setStatut(StatutCommande.INVOICE_CUSTOMER);
        commandeRepository.save(commandeJpa);
        
        log.info("✅ Factures clients simulées et statut mis à jour à INVOICE_CUSTOMER pour la commande {}", 
            commande.getNumeroCommande());
    }

    /**
     * Simule le dépôt du payout (admin valide le payout)
     */
    protected void simulerDepotPayout(CommandeJpa commande) {
        CommandeJpa commandeJpa = commandeRepository.findById(commande.getUuid()).orElseThrow();
        commandeJpa.setDateDepotPayout(LocalDateTime.now());
        commandeJpa.setStatut(StatutCommande.PAYOUT);
        commandeRepository.save(commandeJpa);
    }

    /**
     * Simule l'upload de la facture du marchand
     */
    protected void simulerUploadFactureMarchand(CommandeJpa commande) {
        CommandeJpa commandeJpa = commandeRepository.findById(commande.getUuid()).orElseThrow();
        String factureMarchandUrl = "invoice/seller/facture_" + commande.getNumeroCommande() + ".pdf";
        commandeJpa.setFactureMarchandUrl(factureMarchandUrl);
        commandeJpa.setStatut(StatutCommande.INVOICE_SELLER);
        commandeRepository.save(commandeJpa);
    }

    /**
     * Simule la validation complète par le vendeur
     */
    protected void simulerValidationCompleteVendeur(CommandeJpa commande) {
        // Valider tous les CommandeUtilisateur en changeant leur statut
        CommandeJpa commandeJpa = commandeRepository.findById(commande.getUuid()).orElseThrow();
        if (commandeJpa.getUtilisateursCommande() != null) {
            commandeJpa.getUtilisateursCommande().forEach(cu -> {
                cu.setStatutCommandeUtilisateur(StatutCommandeUtilisateur.VALIDEE);
                commandeUtilisateurRepository.save(cu);
            });
        }
        
        // Changer le statut de la commande
        commandeJpa.setStatut(StatutCommande.TERMINEE);
        commandeRepository.save(commandeJpa);
    }

    /**
     * Vérifie qu'une commande est dans l'état attendu
     */
    protected void verifierStatutCommande(UUID commandeUuid, StatutCommande statutAttendu) {
        CommandeJpa commande = commandeRepository.findById(commandeUuid)
                .orElseThrow(() -> new AssertionError("Commande non trouvée : " + commandeUuid));
        
        if (commande.getStatut() != statutAttendu) {
            throw new AssertionError(String.format(
                "Statut de commande incorrect. Attendu: %s, Obtenu: %s",
                statutAttendu, commande.getStatut()
            ));
        }
    }

    /**
     * Vérifie que tous les CommandeUtilisateur sont validés
     */
    protected void verifierTousValidesParVendeur(UUID commandeUuid) {
        CommandeJpa commande = commandeRepository.findById(commandeUuid)
                .orElseThrow(() -> new AssertionError("Commande non trouvée : " + commandeUuid));
        
        boolean tousValides = commande.getUtilisateursCommande().stream()
                .allMatch(cu -> cu.getStatutCommandeUtilisateur() == StatutCommandeUtilisateur.VALIDEE);
        
        if (!tousValides) {
            throw new AssertionError("Tous les CommandeUtilisateurs ne sont pas validés");
        }
    }
}

