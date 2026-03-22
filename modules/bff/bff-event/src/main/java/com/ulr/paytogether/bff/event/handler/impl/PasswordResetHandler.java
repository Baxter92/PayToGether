package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.core.event.PasswordResetEvent;
import com.ulr.paytogether.core.domaine.service.EmailNotificationService;
import com.ulr.paytogether.core.domaine.service.ValidationTokenService;
import com.ulr.paytogether.core.modele.ValidationTokenModele;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler pour traiter les événements de réinitialisation de mot de passe.
 * Envoie un email avec un token de réinitialisation valide 1h.
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
public class PasswordResetHandler implements ConsumerHandler {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetHandler.class);

    private final EmailNotificationService emailNotificationService;
    private final ValidationTokenService validationTokenService;

    @Value("${app.frontend.base-url:https://dev.dealtogether.ca}")
    private String frontendBaseUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    /**
     * Handler pour traiter l'événement PasswordResetEvent.
     * Envoie un email de réinitialisation à l'utilisateur avec un lien contenant un token.
     *
     * Retry automatique : hérité de ConsumerHandler (3 tentatives, backoff exponentiel)
     *
     * @param event L'événement de réinitialisation de mot de passe
     */
    @FunctionalHandler(
        eventType = PasswordResetEvent.class,
        description = "Envoie un email de réinitialisation de mot de passe avec token 1h"
    )
    public void handlePasswordReset(PasswordResetEvent event) {
        log.info("Handling PasswordResetEvent: utilisateur={}, email={}",
                event.getUtilisateurUuid(), event.getEmail());

        try {
            // 1. Sauvegarder le token via le Service métier (opération critique avec retry)
            ValidationTokenModele tokenModele = ValidationTokenModele.builder()
                    .token(event.getToken())
                    .utilisateurUuid(event.getUtilisateurUuid())
                    .dateExpiration(event.getDateExpiration())
                    .typeToken("REINITIALISATION_MOT_DE_PASSE")
                    .utilise(false)
                    .build();
            validationTokenService.creer(tokenModele);
            log.info("Token de réinitialisation sauvegardé pour utilisateur: {}", event.getUtilisateurUuid());

            // 2. Envoyer l'email UNIQUEMENT après succès de la sauvegarde du token
            // L'email est envoyé en dernier pour éviter les doublons en cas de retry
            envoyerEmailReinitialisation(event);

            log.info("PasswordResetEvent handled successfully for user: {}", event.getUtilisateurUuid());

        } catch (Exception e) {
            log.error("Error handling PasswordResetEvent: {}", e.getMessage(), e);
            throw e; // Propagation pour retry automatique
        }
    }

    /**
     * Envoie l'email de réinitialisation de mot de passe.
     * Cette méthode est appelée UNIQUEMENT après le succès de la sauvegarde du token.
     * En cas d'échec de l'email, l'exception n'est PAS propagée pour éviter un retry complet.
     */
    private void envoyerEmailReinitialisation(PasswordResetEvent event) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("prenom", event.getPrenom());
            variables.put("nom", event.getNom());
            variables.put("token", event.getToken());
            variables.put("lienReinitialisation", construireLienReinitialisation(event.getToken()));
            variables.put("dateExpiration", event.getDateExpiration().format(DATE_FORMATTER));

            emailNotificationService.envoyerNotification(
                    event.getEmail(),
                    "Reset your DealToGether password",
                    "notification-reinitialisation-mot-de-passe-en",
                    variables
            );

            log.info("✅ Email de réinitialisation envoyé à: {}", event.getEmail());
        } catch (Exception e) {
            log.error("⚠️ Échec de l'envoi de l'email de réinitialisation à {}: {}", 
                    event.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Construit le lien de réinitialisation avec le token.
     * Format du lien: {@code https://dev.dealtogether.ca/reset-password?token=xxx}
     */
    private String construireLienReinitialisation(String token) {
        return frontendBaseUrl + "/reset-password?token=" + token;
    }
}

