package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.core.event.PasswordResetEvent;
import com.ulr.paytogether.core.domaine.service.EmailNotificationService;
import com.ulr.paytogether.core.domaine.service.ValidationTokenService;
import com.ulr.paytogether.core.modele.ValidationTokenModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class PasswordResetHandler implements ConsumerHandler {

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
            // 1. Sauvegarder le token via le Service métier
            ValidationTokenModele tokenModele = ValidationTokenModele.builder()
                    .token(event.getToken())
                    .utilisateurUuid(event.getUtilisateurUuid())
                    .dateExpiration(event.getDateExpiration())
                    .typeToken("REINITIALISATION_MOT_DE_PASSE")
                    .utilise(false)
                    .build();
            validationTokenService.creer(tokenModele);
            log.info("Token de réinitialisation sauvegardé pour utilisateur: {}", event.getUtilisateurUuid());

            // 2. Préparer les variables du template
            Map<String, Object> variables = new HashMap<>();
            variables.put("prenom", event.getPrenom());
            variables.put("nom", event.getNom());
            variables.put("token", event.getToken());
            variables.put("lienReinitialisation", construireLienReinitialisation(event.getToken()));
            variables.put("dateExpiration", event.getDateExpiration().format(DATE_FORMATTER));

            // 3. Envoyer l'email via le service métier
            emailNotificationService.envoyerNotification(
                    event.getEmail(),
                    "Reset your DealToGether password",
                    "notification-reinitialisation-mot-de-passe-en",
                    variables
            );

            log.info("PasswordResetEvent handled successfully for user: {}", event.getUtilisateurUuid());

        } catch (Exception e) {
            log.error("Error handling PasswordResetEvent: {}", e.getMessage(), e);
            throw e; // Propagation pour retry automatique
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

