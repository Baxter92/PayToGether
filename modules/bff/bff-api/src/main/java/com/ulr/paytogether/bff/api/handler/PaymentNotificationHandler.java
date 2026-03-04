package com.ulr.paytogether.bff.api.handler;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.bff.event.model.PaymentNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler pour traiter les événements de notification de paiement.
 * Envoie des emails aux utilisateurs concernant leurs paiements.
 *
 * ⚠️ RÈGLE IMPORTANTE :
 * À la fin du traitement de chaque événement, le système publiera AUTOMATIQUEMENT :
 * - HandlerConsumedEvent si le traitement réussit
 * - HandlerFailedEvent si le traitement échoue
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentNotificationHandler implements ConsumerHandler {

    // TODO: Injecter le service d'email quand il sera créé
    // private final EmailService emailService;

    /**
     * Handler pour traiter l'événement PaymentNotificationEvent.
     * Envoie un email de notification à l'utilisateur.
     *
     * @param event L'événement de notification
     */
    @FunctionalHandler(
        eventType = PaymentNotificationEvent.class,
        maxAttempts = 5,
        description = "Envoie des notifications par email pour les paiements"
    )
    public void handlePaymentNotification(PaymentNotificationEvent event) {
        log.info("Handling PaymentNotificationEvent: utilisateur={}, type={}, email={}",
                event.getUtilisateurUuid(), event.getTypeNotification(), event.getEmail());

        try {
            // Vérifier le type de notification
            switch (event.getTypeNotification()) {
                case "EMAIL":
                    envoyerEmail(event);
                    break;
                case "SMS":
                    envoyerSMS(event);
                    break;
                case "PUSH":
                    envoyerNotificationPush(event);
                    break;
                default:
                    log.warn("Type de notification non supporté: {}", event.getTypeNotification());
            }

            log.info("PaymentNotificationEvent handled successfully for user: {}", event.getUtilisateurUuid());

        } catch (Exception e) {
            log.error("Error handling PaymentNotificationEvent: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Envoie un email de notification
     */
    private void envoyerEmail(PaymentNotificationEvent event) {
        log.info("Sending email to: {} with subject: {}", event.getEmail(), event.getSujetNotification());

        // TODO: Implémenter l'envoi d'email réel
        // emailService.send(
        //     event.getEmail(),
        //     event.getSujetNotification(),
        //     event.getMessageNotification()
        // );

        // Pour l'instant, juste logger
        log.info("Email sent successfully to: {}", event.getEmail());
        log.debug("Email content:\nSubject: {}\nMessage: {}",
                 event.getSujetNotification(), event.getMessageNotification());
    }

    /**
     * Envoie un SMS de notification
     */
    private void envoyerSMS(PaymentNotificationEvent event) {
        log.info("Sending SMS to user: {}", event.getUtilisateurUuid());

        // TODO: Implémenter l'envoi de SMS
        // smsService.send(phoneNumber, event.getMessageNotification());

        log.info("SMS sent successfully to user: {}", event.getUtilisateurUuid());
    }

    /**
     * Envoie une notification push
     */
    private void envoyerNotificationPush(PaymentNotificationEvent event) {
        log.info("Sending push notification to user: {}", event.getUtilisateurUuid());

        // TODO: Implémenter l'envoi de notification push
        // pushService.send(event.getUtilisateurUuid(), event.getSujetNotification(), event.getMessageNotification());

        log.info("Push notification sent successfully to user: {}", event.getUtilisateurUuid());
    }
}

