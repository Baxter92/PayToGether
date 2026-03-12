package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.core.event.PaymentMadeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Exemple de handler pour traiter les événements de paiement.
 * Ce handler sera automatiquement découvert et enregistré par le EventConsumerService.
 *
 * ⚠️ RÈGLE IMPORTANTE :
 * À la fin du traitement de chaque événement, le système publiera AUTOMATIQUEMENT :
 * - HandlerConsumedEvent si le traitement réussit
 * - HandlerFailedEvent si le traitement échoue
 *
 * Ces événements de confirmation sont publiés par EventConsumerService,
 * le handler n'a pas besoin de les publier manuellement.
 */
//@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentHandler implements ConsumerHandler {

    // Injection des services nécessaires
    // private final NotificationService notificationService;
    // private final EmailService emailService;

    /**
     * Handler pour traiter l'événement PaymentMadeEvent.
     * Sera exécuté automatiquement quand un PaymentMadeEvent est dispatché.
     *
     * ⚠️ IMPORTANT :
     * - Si ce handler se termine normalement : HandlerConsumedEvent sera publié automatiquement
     * - Si ce handler lance une exception : HandlerFailedEvent sera publié automatiquement
     * - Pas besoin de gérer manuellement la publication de ces événements
     *
     * @param event L'événement de paiement
     */
    @FunctionalHandler(eventType = PaymentMadeEvent.class, maxAttempts = 3, description = "Traite les paiements effectués")
    public void handlePaymentMade(PaymentMadeEvent event) {
        log.info("Handling PaymentMadeEvent: utilisateur={}, commande={}, montant={}",
                event.getUtilisateurUuid(), event.getCommandeUuid(), event.getMontant());

        try {
            // Logique métier après un paiement
            // 1. Envoyer une notification à l'utilisateur
            // notificationService.envoyerNotificationPaiement(event.getUtilisateurUuid(), event.getMontant());

            // 2. Envoyer un email de confirmation
            // emailService.envoyerConfirmationPaiement(event);

            // 3. Mettre à jour le statut de la commande
            // commandeService.marquerCommePayee(event.getCommandeUuid());

            // 4. Déclencher le processus de préparation
            // preparationService.demarrerPreparation(event.getCommandeUuid());

            log.info("PaymentMadeEvent handled successfully for commande: {}", event.getCommandeUuid());

            // ✅ À la sortie normale de cette méthode :
            // → HandlerConsumedEvent sera publié automatiquement avec :
            //   - originalEventId = event.getEventId()
            //   - originalEventType = "PaymentMadeEvent"
            //   - handlerName = "PaymentHandler.handlePaymentMade"

        } catch (Exception e) {
            log.error("Error handling PaymentMadeEvent: {}", e.getMessage(), e);

            // ✅ En relançant l'exception :
            // → HandlerFailedEvent sera publié automatiquement avec :
            //   - originalEventId = event.getEventId()
            //   - errorMessage = e.getMessage()
            //   - attemptNumber = tentative actuelle
            //   - isFinalFailure = true si c'est la 3ème tentative

            throw e; // Relancer pour déclencher le retry
        }
    }

    /**
     * Autre exemple de handler pour le même événement
     * Plusieurs handlers peuvent traiter le même événement
     *
     * ⚠️ IMPORTANT : Même règle de publication automatique d'événements
     */
    @FunctionalHandler(eventType = PaymentMadeEvent.class, maxAttempts = 3, description = "Mise à jour des statistiques")
    public void updateStatistics(PaymentMadeEvent event) {
        log.info("Updating payment statistics for amount: {}", event.getMontant());

        // Logique de mise à jour des statistiques
        // statisticsService.incrementerPaiements(event.getMontant());

        log.info("Statistics updated successfully");

        // ✅ HandlerConsumedEvent sera publié automatiquement
    }
}

