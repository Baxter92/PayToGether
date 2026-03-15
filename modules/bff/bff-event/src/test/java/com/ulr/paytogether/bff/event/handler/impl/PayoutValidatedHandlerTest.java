package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.core.domaine.service.EmailNotificationService;
import com.ulr.paytogether.core.event.PayoutValidatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PayoutValidatedHandler
 * 
 * ✅ Mock le service EmailNotificationService
 * ✅ Teste l'envoi d'email au vendeur
 * ✅ Vérifie les paramètres du template
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires : PayoutValidatedHandler")
class PayoutValidatedHandlerTest {

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private PayoutValidatedHandler handler;

    private String frontendBaseUrl;
    private UUID commandeUuid;
    private UUID vendeurUuid;
    private String numeroCommande;
    private String emailVendeur;
    private String nomVendeur;
    private LocalDateTime dateDepotPayout;

    @BeforeEach
    void setUp() {
        frontendBaseUrl = "https://dealtogether.ca";
        commandeUuid = UUID.randomUUID();
        vendeurUuid = UUID.randomUUID();
        numeroCommande = "CMD-123456";
        emailVendeur = "vendeur@example.com";
        nomVendeur = "Jean Vendeur";
        dateDepotPayout = LocalDateTime.of(2026, 3, 15, 10, 30);

        // Injecter la valeur de la propriété @Value via ReflectionTestUtils
        ReflectionTestUtils.setField(handler, "frontendBaseUrl", frontendBaseUrl);
    }

    @Test
    @DisplayName("handlePayoutValidated : Devrait envoyer email au vendeur avec les bons paramètres")
    void handlePayoutValidated_DevraitEnvoyerEmailAuVendeur() {
        // Given
        PayoutValidatedEvent event = new PayoutValidatedEvent(commandeUuid, numeroCommande, vendeurUuid, emailVendeur, nomVendeur, dateDepotPayout
        );

        doNothing().when(emailNotificationService).envoyerEmailAvecTemplate(
            anyString(), anyString(), anyString(), anyMap());

        // When
        assertDoesNotThrow(() -> handler.handlePayoutValidated(event));

        // Then
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(emailNotificationService).envoyerEmailAvecTemplate(
            emailCaptor.capture(),
            subjectCaptor.capture(),
            templateCaptor.capture(),
            paramsCaptor.capture()
        );

        // Vérifier l'email du destinataire
        assertEquals(emailVendeur, emailCaptor.getValue());

        // Vérifier le sujet
        assertTrue(subjectCaptor.getValue().contains("Payout Validated"));

        // Vérifier le template
        assertEquals("payout-validated", templateCaptor.getValue());

        // Vérifier les paramètres du template
        Map<String, Object> params = paramsCaptor.getValue();
        assertEquals(nomVendeur, params.get("vendeurNom"));
        assertEquals(numeroCommande, params.get("numeroCommande"));
        assertEquals("2026-03-15 10:30:00", params.get("dateDepotPayout"));
        assertEquals(frontendBaseUrl + "/vendeur/commandes/" + commandeUuid + "/facture", 
            params.get("uploadUrl"));
    }

    @Test
    @DisplayName("handlePayoutValidated : Devrait construire la bonne URL d'upload")
    void handlePayoutValidated_DevraitConstruireBonneUrlUpload() {
        // Given
        PayoutValidatedEvent event = new PayoutValidatedEvent(commandeUuid, numeroCommande, vendeurUuid, emailVendeur, nomVendeur, dateDepotPayout
        );

        doNothing().when(emailNotificationService).envoyerEmailAvecTemplate(
            anyString(), anyString(), anyString(), anyMap());

        // When
        assertDoesNotThrow(() -> handler.handlePayoutValidated(event));

        // Then
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailNotificationService).envoyerEmailAvecTemplate(
            anyString(), anyString(), anyString(), paramsCaptor.capture());

        Map<String, Object> params = paramsCaptor.getValue();
        String expectedUrl = frontendBaseUrl + "/vendeur/commandes/" + commandeUuid + "/facture";
        assertEquals(expectedUrl, params.get("uploadUrl"));
    }

    @Test
    @DisplayName("handlePayoutValidated : Devrait lancer exception en cas d'erreur d'envoi email")
    void handlePayoutValidated_DevraitLancerExceptionEnCasErreurEmail() {
        // Given
        PayoutValidatedEvent event = new PayoutValidatedEvent(commandeUuid, numeroCommande, vendeurUuid, emailVendeur, nomVendeur, dateDepotPayout
        );

        doThrow(new RuntimeException("Erreur SMTP"))
            .when(emailNotificationService).envoyerEmailAvecTemplate(
                anyString(), anyString(), anyString(), anyMap());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> handler.handlePayoutValidated(event));

        assertTrue(exception.getMessage().contains("Échec de l'envoi de l'email"));
        verify(emailNotificationService).envoyerEmailAvecTemplate(
            eq(emailVendeur), anyString(), eq("payout-validated"), anyMap());
    }

    @Test
    @DisplayName("handlePayoutValidated : Devrait formater correctement la date")
    void handlePayoutValidated_DevraitFormaterCorrectementDate() {
        // Given
        LocalDateTime dateSpecifique = LocalDateTime.of(2026, 12, 25, 14, 45, 30);
        PayoutValidatedEvent event = new PayoutValidatedEvent(
            commandeUuid,
            numeroCommande,
            vendeurUuid,
            emailVendeur,
            nomVendeur,
            dateSpecifique
        );

        doNothing().when(emailNotificationService).envoyerEmailAvecTemplate(
            anyString(), anyString(), anyString(), anyMap());

        // When
        assertDoesNotThrow(() -> handler.handlePayoutValidated(event));

        // Then
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailNotificationService).envoyerEmailAvecTemplate(
            anyString(), anyString(), anyString(), paramsCaptor.capture());

        Map<String, Object> params = paramsCaptor.getValue();
        // Format attendu : yyyy-MM-dd HH:mm:ss
        assertEquals("2026-12-25 14:45:30", params.get("dateDepotPayout"));
    }

    @Test
    @DisplayName("handlePayoutValidated : Devrait appeler le service une seule fois")
    void handlePayoutValidated_DevraitAppelerServiceUneSeuleFois() {
        // Given
        PayoutValidatedEvent event = new PayoutValidatedEvent(commandeUuid, numeroCommande, vendeurUuid, emailVendeur, nomVendeur, dateDepotPayout
        );

        doNothing().when(emailNotificationService).envoyerEmailAvecTemplate(
            anyString(), anyString(), anyString(), anyMap());

        // When
        assertDoesNotThrow(() -> handler.handlePayoutValidated(event));

        // Then
        verify(emailNotificationService, times(1)).envoyerEmailAvecTemplate(
            anyString(), anyString(), anyString(), anyMap());
    }
}

