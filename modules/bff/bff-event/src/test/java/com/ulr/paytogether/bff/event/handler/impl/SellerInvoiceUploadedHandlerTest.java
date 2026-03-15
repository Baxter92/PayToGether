package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.core.domaine.service.*;
import com.ulr.paytogether.core.enumeration.StatutCommande;
import com.ulr.paytogether.core.event.SellerInvoiceUploadedEvent;
import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour SellerInvoiceUploadedHandler
 * 
 * ✅ Mock toutes les dépendances (Services du core)
 * ✅ Teste la génération et l'envoi des factures clients
 * ✅ Vérifie la mise à jour du statut de commande
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires : SellerInvoiceUploadedHandler")
class SellerInvoiceUploadedHandlerTest {

    @Mock
    private CommandeService commandeService;

    @Mock
    private PaiementService paiementService;

    @Mock
    private InvoiceGeneratorService invoiceGeneratorService;

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private SellerInvoiceUploadedHandler handler;

    private UUID commandeUuid;
    private String numeroCommande;
    private String factureMarchandUrl;
    private CommandeModele commandeModele;
    private List<PaiementModele> paiements;
    private DealModele dealModele;

    @BeforeEach
    void setUp() {
        commandeUuid = UUID.randomUUID();
        numeroCommande = "CMD-123456";
        factureMarchandUrl = "invoice/seller/facture_CMD-123456.pdf";

        // Setup Deal
        dealModele = new DealModele();
        dealModele.setUuid(UUID.randomUUID());
        dealModele.setTitre("Deal Test");
        dealModele.setDescription("Description du deal");

        // Setup Commande
        commandeModele = new CommandeModele();
        commandeModele.setUuid(commandeUuid);
        commandeModele.setNumeroCommande(numeroCommande);
        commandeModele.setDealModele(dealModele);

        // Setup Paiements (3 clients)
        paiements = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            UtilisateurModele utilisateur = new UtilisateurModele();
            utilisateur.setUuid(UUID.randomUUID());
            utilisateur.setEmail("client" + i + "@example.com");
            utilisateur.setPrenom("Client" + i);
            utilisateur.setNom("Test");

            PaiementModele paiement = new PaiementModele();
            paiement.setUuid(UUID.randomUUID());
            paiement.setMontant(BigDecimal.valueOf(100.00));
            paiement.setUtilisateur(utilisateur);
            paiement.setCommande(commandeModele);

            paiements.add(paiement);
        }
    }

    @Test
    @DisplayName("handleSellerInvoiceUploaded : Devrait générer et envoyer toutes les factures clients")
    void handleSellerInvoiceUploaded_DevraitGenererEtEnvoyerToutesLesFactures() throws Exception {
        // Given
        SellerInvoiceUploadedEvent event = new SellerInvoiceUploadedEvent(
            commandeUuid,
            numeroCommande,
            factureMarchandUrl
        );

        byte[] fakePdfContent = "PDF Content".getBytes();

        when(commandeService.lireParUuid(commandeUuid)).thenReturn(commandeModele);
        when(paiementService.lireParCommande(commandeUuid)).thenReturn(paiements);
        when(invoiceGeneratorService.genererFactureClient(
            any(PaiementModele.class), anyString(), anyString(), anyBoolean(), anyString()))
            .thenReturn(fakePdfContent);
        when(fileStorageService.generateReadUrl(anyString()))
            .thenReturn("https://minio.test/invoice_url");
        // uploadFile et mettreAJourStatutCommande ne nécessitent pas de mock car pas de vérification de retour
        doNothing().when(emailNotificationService).envoyerEmailAvecPieceJointe(
            anyString(), anyString(), anyString(), anyMap(), any(byte[].class), anyString());

        // When
        assertDoesNotThrow(() -> handler.handleSellerInvoiceUploaded(event));

        // Then
        verify(commandeService).lireParUuid(commandeUuid);
        verify(paiementService).lireParCommande(commandeUuid);
        
        // Vérifier que 3 factures ont été générées (une par paiement)
        verify(invoiceGeneratorService, times(3)).genererFactureClient(
            any(PaiementModele.class), eq(numeroCommande), eq("Deal Test"), 
            anyBoolean(), anyString());
        
        // Vérifier que 3 factures ont été uploadées dans MinIO
        verify(fileStorageService, times(3)).uploadFile(
            any(ByteArrayInputStream.class), anyString(), eq("invoice/user/"), eq((long) fakePdfContent.length));
        
        // Vérifier que 3 emails ont été envoyés
        verify(emailNotificationService, times(3)).envoyerEmailAvecPieceJointe(
            anyString(), contains("Your Invoice"), eq("invoice-client-email"), 
            anyMap(), eq(fakePdfContent), anyString());
        
        // Vérifier que le statut de la commande a été mis à jour
        verify(commandeService).mettreAJourStatutCommande(
            commandeUuid, StatutCommande.INVOICE_CUSTOMER);
    }

    @Test
    @DisplayName("handleSellerInvoiceUploaded : Devrait lancer exception si commande non trouvée")
    void handleSellerInvoiceUploaded_DevraitLancerExceptionSiCommandeNonTrouvee() throws Exception {
        // Given
        SellerInvoiceUploadedEvent event = new SellerInvoiceUploadedEvent(
            commandeUuid,
            numeroCommande,
            factureMarchandUrl
        );

        when(commandeService.lireParUuid(commandeUuid)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> handler.handleSellerInvoiceUploaded(event));

        assertTrue(exception.getMessage().contains("Échec de la génération"));
        verify(paiementService, never()).lireParCommande(any());
        verify(invoiceGeneratorService, never()).genererFactureClient(
            any(), anyString(), anyString(), anyBoolean(), anyString());
    }

    @Test
    @DisplayName("handleSellerInvoiceUploaded : Devrait gérer le cas sans paiements")
    void handleSellerInvoiceUploaded_DevraitGererCasSansPaiements() throws Exception {
        // Given
        SellerInvoiceUploadedEvent event = new SellerInvoiceUploadedEvent(
            commandeUuid,
            numeroCommande,
            factureMarchandUrl
        );

        when(commandeService.lireParUuid(commandeUuid)).thenReturn(commandeModele);
        when(paiementService.lireParCommande(commandeUuid)).thenReturn(Collections.emptyList());

        // When
        assertDoesNotThrow(() -> handler.handleSellerInvoiceUploaded(event));

        // Then
        verify(invoiceGeneratorService, never()).genererFactureClient(
            any(), anyString(), anyString(), anyBoolean(), anyString());
        verify(emailNotificationService, never()).envoyerEmailAvecPieceJointe(
            anyString(), anyString(), anyString(), anyMap(), any(byte[].class), anyString());
        verify(commandeService, never()).mettreAJourStatutCommande(any(), any());
    }

    @Test
    @DisplayName("handleSellerInvoiceUploaded : Devrait continuer même si une facture échoue")
    void handleSellerInvoiceUploaded_DevraitContinuerMemeSiUneFactureEchoue() throws Exception {
        // Given
        SellerInvoiceUploadedEvent event = new SellerInvoiceUploadedEvent(
            commandeUuid,
            numeroCommande,
            factureMarchandUrl
        );

        byte[] fakePdfContent = "PDF Content".getBytes();

        when(commandeService.lireParUuid(commandeUuid)).thenReturn(commandeModele);
        when(paiementService.lireParCommande(commandeUuid)).thenReturn(paiements);
        
        // Simuler un échec sur le 2ème paiement
        when(invoiceGeneratorService.genererFactureClient(
            any(PaiementModele.class), anyString(), anyString(), anyBoolean(), anyString()))
            .thenReturn(fakePdfContent)
            .thenThrow(new RuntimeException("Erreur génération facture"))
            .thenReturn(fakePdfContent);
        
        when(fileStorageService.generateReadUrl(anyString()))
            .thenReturn("https://minio.test/invoice_url");
        // uploadFile et mettreAJourStatutCommande ne nécessitent pas de mock
        doNothing().when(emailNotificationService).envoyerEmailAvecPieceJointe(
            anyString(), anyString(), anyString(), anyMap(), any(byte[].class), anyString());

        // When
        assertDoesNotThrow(() -> handler.handleSellerInvoiceUploaded(event));

        // Then - 2 factures sur 3 devraient être envoyées
        verify(invoiceGeneratorService, times(3)).genererFactureClient(
            any(PaiementModele.class), anyString(), anyString(), anyBoolean(), anyString());
        verify(emailNotificationService, times(2)).envoyerEmailAvecPieceJointe(
            anyString(), anyString(), anyString(), anyMap(), any(byte[].class), anyString());
        verify(commandeService).mettreAJourStatutCommande(
            commandeUuid, StatutCommande.INVOICE_CUSTOMER);
    }

    @Test
    @DisplayName("handleSellerInvoiceUploaded : Devrait lancer exception si aucune facture n'est envoyée")
    void handleSellerInvoiceUploaded_DevraitLancerExceptionSiAucuneFactureEnvoyee() throws Exception {
        // Given
        SellerInvoiceUploadedEvent event = new SellerInvoiceUploadedEvent(
            commandeUuid,
            numeroCommande,
            factureMarchandUrl
        );

        when(commandeService.lireParUuid(commandeUuid)).thenReturn(commandeModele);
        when(paiementService.lireParCommande(commandeUuid)).thenReturn(paiements);
        
        // Simuler un échec sur tous les paiements
        when(invoiceGeneratorService.genererFactureClient(
            any(PaiementModele.class), anyString(), anyString(), anyBoolean(), anyString()))
            .thenThrow(new RuntimeException("Erreur génération facture"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> handler.handleSellerInvoiceUploaded(event));

        assertTrue(exception.getMessage().contains("Échec de la génération"), 
            "Le message devrait contenir 'Échec de la génération', message reçu: " + exception.getMessage());
        verify(commandeService, never()).mettreAJourStatutCommande(any(), any());
    }

    @Test
    @DisplayName("handleSellerInvoiceUploaded : Devrait vérifier les paramètres d'email")
    void handleSellerInvoiceUploaded_DevraitVerifierParametresEmail() throws Exception {
        // Given
        SellerInvoiceUploadedEvent event = new SellerInvoiceUploadedEvent(
            commandeUuid,
            numeroCommande,
            factureMarchandUrl
        );

        byte[] fakePdfContent = "PDF Content".getBytes();
        String invoiceUrl = "https://minio.test/invoice_url_12345";

        when(commandeService.lireParUuid(commandeUuid)).thenReturn(commandeModele);
        when(paiementService.lireParCommande(commandeUuid)).thenReturn(List.of(paiements.get(0)));
        when(invoiceGeneratorService.genererFactureClient(
            any(PaiementModele.class), anyString(), anyString(), anyBoolean(), anyString()))
            .thenReturn(fakePdfContent);
        when(fileStorageService.generateReadUrl(anyString())).thenReturn(invoiceUrl);
        // uploadFile et mettreAJourStatutCommande ne nécessitent pas de mock
        doNothing().when(emailNotificationService).envoyerEmailAvecPieceJointe(
            anyString(), anyString(), anyString(), anyMap(), any(byte[].class), anyString());

        // When
        assertDoesNotThrow(() -> handler.handleSellerInvoiceUploaded(event));

        // Then - Vérifier les paramètres du template email
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailNotificationService).envoyerEmailAvecPieceJointe(
            eq("client1@example.com"),
            eq("Your Invoice - Order " + numeroCommande),
            eq("invoice-client-email"),
            paramsCaptor.capture(),
            eq(fakePdfContent),
            anyString()
        );

        Map<String, Object> params = paramsCaptor.getValue();
        assertEquals("Client1 Test", params.get("clientNom"));
        assertEquals(numeroCommande, params.get("numeroCommande"));
        assertEquals("Deal Test", params.get("dealTitre"));
        assertEquals(BigDecimal.valueOf(100.00), params.get("montantTotal"));
        assertEquals(invoiceUrl, params.get("invoiceUrl"));
    }
}
