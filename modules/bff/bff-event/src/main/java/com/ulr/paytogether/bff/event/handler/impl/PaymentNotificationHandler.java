package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.core.domaine.service.UtilisateurService;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import com.ulr.paytogether.core.event.PaymentNotificationEvent;
import com.ulr.paytogether.core.domaine.service.EmailNotificationService;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.ulr.paytogether.bff.event.utils.EventUtils.DATE_FORMATTER;

/**
 * Handler pour traiter les événements de notification de paiement.
 * Envoie des emails aux utilisateurs concernant leurs paiements.
 *
 * ⚠️ ARCHITECTURE HEXAGONALE :
 * - Ce handler fait partie de la PARTIE GAUCHE (bff-event - adaptateur d'entrée)
 * - Il appelle uniquement les SERVICES du CORE (bff-core)
 * - Il ne doit JAMAIS accéder directement aux Providers ou Repositories
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

    private final EmailNotificationService emailNotificationService;
    private final UtilisateurService utilisateurService;

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
     * Envoie un email de notification via le Service métier
     */
    private void envoyerEmail(PaymentNotificationEvent event) {
        log.info("Sending email to: {} with subject: {}", event.getEmail(), event.getSujetNotification());

        var isCondirmed = StatutPaiement.CONFIRME.name().equals(event.getStatutPaiement());

        UtilisateurModele utilisateur = utilisateurService.lireParUuid(event.getUtilisateurUuid()).orElse(null);
        Map<String, Object> variables = new HashMap<>();
        variables.put("nom", Optional.ofNullable(utilisateur).map(UtilisateurModele::getNom).orElse(""));
        variables.put("prenom", Optional.ofNullable(utilisateur).map(UtilisateurModele::getPrenom).orElse(""));
        variables.put("titreDeal", event.getTitreDeal());
        variables.put("montant", event.getMontantPaiement());
        if (isCondirmed) {
            variables.put("datePaiement", event.getDatePaiement().format(DATE_FORMATTER));
            variables.put("methodePaiement", event.getMethodePaiement());
            variables.put("descriptionDeal", event.getDescriptionDeal());
        }else {
            variables.put("dateTentative", event.getDatePaiement());
            variables.put("raisonEchec", "Veuillez vérifier les informations de votre carte ou contacter votre banque.");
        }

        String template = isCondirmed ? "notification-paiement-reussi" : "notification-paiement-echoue";
        // Appeler le service métier pour envoyer l'email
        emailNotificationService.envoyerNotification(
            event.getEmail(),
            event.getSujetNotification(),
            template,
            variables
        );

        log.info("Email sent successfully to: {}", event.getEmail());
    }

    /**
     * Envoie un SMS de notification
     * TODO: Créer un service SMS dans bff-core et l'utiliser ici
     */
    private void envoyerSMS(PaymentNotificationEvent event) {
        log.info("Sending SMS to user: {}", event.getUtilisateurUuid());
        // TODO: Implémenter avec un service SMS du core
        log.warn("SMS not implemented yet - user: {}", event.getUtilisateurUuid());
    }

    /**
     * Envoie une notification push
     * TODO: Créer un service Push dans bff-core et l'utiliser ici
     */
    private void envoyerNotificationPush(PaymentNotificationEvent event) {
        log.info("Sending push notification to user: {}", event.getUtilisateurUuid());
        // TODO: Implémenter avec un service Push du core
        log.warn("Push notification not implemented yet - user: {}", event.getUtilisateurUuid());
    }
}

