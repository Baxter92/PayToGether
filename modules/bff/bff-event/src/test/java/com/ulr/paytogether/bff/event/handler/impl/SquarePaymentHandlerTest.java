package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.core.domaine.service.*;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import com.ulr.paytogether.core.event.EventPublisher;
import com.ulr.paytogether.core.event.PaymentFailedEvent;
import com.ulr.paytogether.core.event.PaymentInitiatedEvent;
import com.ulr.paytogether.core.event.PaymentSuccessfulEvent;
import com.ulr.paytogether.core.modele.AdresseModele;
import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.core.provider.AdresseProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour SquarePaymentHandler
 * 
 * ✅ Mock toutes les dépendances (Services du core)
 * ✅ Teste chaque méthode du handler isolément
 * ✅ Vérifie les interactions avec les services
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires : SquarePaymentHandler")
class SquarePaymentHandlerTest {

    @Mock
    private SquarePaymentService squarePaymentService;

    @Mock
    private PaiementService paiementService;

    @Mock
    private CommandeService commandeService;

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private UtilisateurService utilisateurService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private AdresseProvider adresseProvider;

    @Mock
    private DealParticipantService dealParticipantService;

    @InjectMocks
    private SquarePaymentHandler handler;

    private UUID utilisateurUuid;
    private UUID commandeUuid;
    private UUID paiementUuid;
    private UUID dealUuid;
    private PaiementModele paiementModele;
    private UtilisateurModele utilisateurModele;
    private CommandeModele commandeModele;
    private DealModele dealModele;

    @BeforeEach
    void setUp() {
        utilisateurUuid = UUID.randomUUID();
        commandeUuid = UUID.randomUUID();
        paiementUuid = UUID.randomUUID();
        dealUuid = UUID.randomUUID();

        // Setup utilisateur
        utilisateurModele = new UtilisateurModele();
        utilisateurModele.setUuid(utilisateurUuid);
        utilisateurModele.setEmail("test@example.com");
        utilisateurModele.setPrenom("John");
        utilisateurModele.setNom("Doe");

        // Setup deal
        dealModele = new DealModele();
        dealModele.setUuid(dealUuid);
        dealModele.setTitre("Deal Test");
        dealModele.setDescription("Description du deal");
        dealModele.setNbParticipants(3);
        dealModele.setCreateur(utilisateurModele);

        // Setup commande
        commandeModele = new CommandeModele();
        commandeModele.setUuid(commandeUuid);
        commandeModele.setNumeroCommande("CMD-123456");
        commandeModele.setDealModele(dealModele);

        // Setup paiement
        paiementModele = new PaiementModele();
        paiementModele.setUuid(paiementUuid);
        paiementModele.setMontant(BigDecimal.valueOf(100.00));
        paiementModele.setUtilisateur(utilisateurModele);
        paiementModele.setCommande(commandeModele);
        paiementModele.setDeal(dealModele);
        paiementModele.setSquarePaymentId("sq_test_123");
        paiementModele.setSquareReceiptUrl("https://squareup.com/receipt/test");
    }

    @Test
    @DisplayName("handlePaymentInitiated : Devrait traiter le paiement avec succès")
    void handlePaymentInitiated_DevraitTraiterPaiementAvecSucces() {
        // Given
        PaymentInitiatedEvent event = new PaymentInitiatedEvent(
            utilisateurUuid,
            commandeUuid,
            paiementUuid,
            BigDecimal.valueOf(100.00),
            "SQUARE_CARD",
            1,
            "cnon:test_token"
        );

        when(paiementService.lireParUuid(paiementUuid)).thenReturn(Optional.of(paiementModele));
        // traiterPaiementSquare ne retourne rien mais n'est pas void non plus dans la signature
        // On ne mock rien du tout, juste on vérifie que la méthode est appelée

        // When
        assertDoesNotThrow(() -> handler.handlePaymentInitiated(event));

        // Then
        verify(paiementService).lireParUuid(paiementUuid);
        verify(squarePaymentService).traiterPaiementSquare(paiementModele);
        assertEquals(1, paiementModele.getNombreDePart());
    }

    @Test
    @DisplayName("handlePaymentInitiated : Devrait lancer exception si paiement non trouvé")
    void handlePaymentInitiated_DevraitLancerExceptionSiPaiementNonTrouve() {
        // Given
        PaymentInitiatedEvent event = new PaymentInitiatedEvent(
            utilisateurUuid,
            commandeUuid,
            paiementUuid,
            BigDecimal.valueOf(100.00),
            "SQUARE_CARD",
            1,
            "cnon:test_token"
        );

        when(paiementService.lireParUuid(paiementUuid)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> handler.handlePaymentInitiated(event));
        
        assertTrue(exception.getMessage().contains("Paiement non trouvé"));
        verify(squarePaymentService, never()).traiterPaiementSquare(any());
    }

    @Test
    @DisplayName("handlePaymentSuccessful : Devrait mettre à jour statut et envoyer notification")
    void handlePaymentSuccessful_DevraitMettreAJourStatutEtEnvoyerNotification() {
        // Given
        PaymentSuccessfulEvent event = new PaymentSuccessfulEvent(
            utilisateurUuid,
            commandeUuid,
            paiementUuid,
            BigDecimal.valueOf(100.00),
            "SQUARE_CARD",
            "sq_payment_123",
            1,
            "https://squareup.com/receipt/test"
        );

        when(commandeService.lireParUuid(commandeUuid)).thenReturn(commandeModele);
        when(utilisateurService.lireParUuid(utilisateurUuid)).thenReturn(Optional.of(utilisateurModele));
        when(dealParticipantService.compterNombreParts(dealUuid)).thenReturn(2L);
        doNothing().when(squarePaymentService).mettreAJourStatutCommandeDeal(
            any(UUID.class), anyString(), anyInt());
        doNothing().when(emailNotificationService).envoyerNotification(
            anyString(), anyString(), anyString(), anyMap());

        // When
        assertDoesNotThrow(() -> handler.handlePaymentSuccessful(event));

        // Then
        verify(squarePaymentService).mettreAJourStatutCommandeDeal(
            paiementUuid, 
            StatutPaiement.CONFIRME.name(), 
            1
        );
        verify(emailNotificationService).envoyerNotification(
            eq("test@example.com"),
            eq("New participant - Deal Test"),
            eq("notification-nouveau-participant-en"),
            any(HashMap.class)
        );
    }

    @Test
    @DisplayName("handlePaymentFailed : Devrait envoyer email d'échec")
    void handlePaymentFailed_DevraitEnvoyerEmailEchec() {
        // Given
        PaymentFailedEvent event = new PaymentFailedEvent(
            utilisateurUuid,
            commandeUuid,
            paiementUuid,
            BigDecimal.valueOf(100.00),
            "SQUARE_CARD",
            "Card declined",
            "CARD_DECLINED",
            "test@example.com",
            "John",
            "Doe",
            "Deal Test",
            "Description",
            1,
            3,
            "123 Test St",
            "Montreal",
            "QC",
            "H1A1A1",
            "Canada"
        );

        doNothing().when(emailNotificationService).envoyerNotification(
            anyString(), anyString(), anyString(), anyMap());

        // When
        assertDoesNotThrow(() -> handler.handlePaymentFailed(event));

        // Then
        ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailNotificationService).envoyerNotification(
            eq("test@example.com"),
            contains("chec de paiement"),
            eq("notification-paiement-echoue-en"),
            variablesCaptor.capture()
        );

        Map<String, Object> variables = variablesCaptor.getValue();
        assertEquals("John", variables.get("prenom"));
        assertEquals("Doe", variables.get("nom"));
        // Le montant peut être BigDecimal ou Double selon l'implémentation
        Object montant = variables.get("montant");
        assertTrue(montant instanceof Number, "Le montant devrait être un Number");
        assertEquals(100.0, ((Number) montant).doubleValue(), 0.01);
    }

    @Test
    @DisplayName("recoverFromPaymentInitiatedFailure : Devrait publier PaymentFailedEvent après échec définitif")
    void recoverFromPaymentInitiatedFailure_DevraitPublierPaymentFailedEvent() {
        // Given
        PaymentInitiatedEvent event = new PaymentInitiatedEvent(
            utilisateurUuid,
            commandeUuid,
            paiementUuid,
            BigDecimal.valueOf(100.00),
            "SQUARE_CARD",
            1,
            "cnon:test_token"
        );

        AdresseModele adresseModele = new AdresseModele();
        adresseModele.setRue("123 Test St");
        adresseModele.setVille("Montreal");
        adresseModele.setProvince("QC");
        adresseModele.setCodePostal("H1A1A1");
        adresseModele.setPays("Canada");

        when(paiementService.lireParUuid(paiementUuid)).thenReturn(Optional.of(paiementModele));
        when(utilisateurService.lireParUuid(utilisateurUuid)).thenReturn(Optional.of(utilisateurModele));
        when(adresseProvider.trouverParPaiement(paiementUuid)).thenReturn(adresseModele);
        doNothing().when(eventPublisher).publishAsync(any(PaymentFailedEvent.class));

        Exception originalException = new RuntimeException("Payment processing error");

        // When
        assertDoesNotThrow(() -> handler.recoverFromPaymentInitiatedFailure(originalException, event));

        // Then
        ArgumentCaptor<PaymentFailedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(eventPublisher).publishAsync(eventCaptor.capture());

        PaymentFailedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(utilisateurUuid, publishedEvent.getUtilisateurUuid());
        assertEquals(commandeUuid, publishedEvent.getCommandeUuid());
        assertEquals(paiementUuid, publishedEvent.getPaiementUuid());
        assertEquals("Payment processing error", publishedEvent.getMessageErreur());
        assertEquals("test@example.com", publishedEvent.getEmail());
    }

    @Test
    @DisplayName("handlePaymentSuccessful : Ne devrait pas planter si utilisateur introuvable")
    void handlePaymentSuccessful_NePasPlanterSiUtilisateurIntrouvable() {
        // Given
        PaymentSuccessfulEvent event = new PaymentSuccessfulEvent(
            utilisateurUuid,
            commandeUuid,
            paiementUuid,
            BigDecimal.valueOf(100.00),
            "SQUARE_CARD",
            "sq_payment_123",
            1,
            "https://squareup.com/receipt/test"
        );

        when(commandeService.lireParUuid(commandeUuid)).thenReturn(commandeModele);
        when(utilisateurService.lireParUuid(utilisateurUuid)).thenReturn(Optional.empty());
        when(dealParticipantService.compterNombreParts(dealUuid)).thenReturn(2L);
        doNothing().when(squarePaymentService).mettreAJourStatutCommandeDeal(
            any(UUID.class), anyString(), anyInt());
        doNothing().when(emailNotificationService).envoyerNotification(
            anyString(), anyString(), anyString(), anyMap());

        // When & Then
        assertDoesNotThrow(() -> handler.handlePaymentSuccessful(event));
        
        verify(emailNotificationService).envoyerNotification(
            anyString(), anyString(), anyString(), any(HashMap.class)
        );
    }
}

