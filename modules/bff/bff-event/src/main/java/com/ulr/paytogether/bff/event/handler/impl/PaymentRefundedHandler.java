package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.core.domaine.service.EmailNotificationService;
import com.ulr.paytogether.core.domaine.service.SquarePaymentService;
import com.ulr.paytogether.core.event.PaymentRefundedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler pour traiter les événements de remboursement de paiement.
 *
 * Actions effectuées :
 * 1. Envoie un email de confirmation de remboursement à l'utilisateur
 * 2. Supprime la participation de l'utilisateur au deal
 * 3. Supprime le paiement et potentiellement la commande
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRefundedHandler implements ConsumerHandler {

    private final EmailNotificationService emailNotificationService;
    private final SquarePaymentService squarePaymentService;

    /**
     * Handler pour traiter l'événement PaymentRefundedEvent.
     * Envoie un email de confirmation et supprime la participation.
     *
     * @param event L'événement de remboursement
     */
    @FunctionalHandler(
        eventType = PaymentRefundedEvent.class,
        maxAttempts = 3,
        description = "Gestion des remboursements : email + suppression participation"
    )
    public void handlePaymentRefunded(PaymentRefundedEvent event) {
        log.info("Handling PaymentRefundedEvent: paiement={}, utilisateur={}, montant={}",
                event.getPaiementUuid(), event.getUtilisateurUuid(), event.getMontantRembourse());

        try {
            // 1. Envoyer l'email de confirmation de remboursement
            Map<String, Object> variables = new HashMap<>();
            variables.put("prenom", event.getPrenom());
            variables.put("nom", event.getNom());
            variables.put("montant", event.getMontantRembourse());
            variables.put("titreDeal", event.getTitreDeal());
            variables.put("descriptionDeal", event.getDescriptionDeal());
            variables.put("dateRemboursement", event.getDateRemboursement().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            variables.put("raisonRemboursement", event.getRaisonRemboursement() != null
                    ? event.getRaisonRemboursement()
                    : "Remboursement demandé par l'administrateur");
            variables.put("refundId", event.getRefundId());
            variables.put("supportEmail", "support@dealtogether.ca");

            emailNotificationService.envoyerNotification(
                    event.getEmail(),
                    "Remboursement confirmé - " + event.getTitreDeal(),
                    "notification-paiement-rembourse-en",
                    variables
            );

            log.info("✅ Email de remboursement envoyé à: {}", event.getEmail());

            // 2. Supprimer la participation de l'utilisateur au deal
            squarePaymentService.supprimerParticipationApresRemboursement(
                    event.getUtilisateurUuid(),
                    event.getDealUuid(),
                    event.getNombreDeParts()
            );

            log.info("✅ PaymentRefundedEvent handled successfully for paiement: {}", event.getPaiementUuid());

        } catch (Exception e) {
            log.error("❌ Error handling PaymentRefundedEvent: {}", e.getMessage(), e);
            throw e;
        }
    }
}

