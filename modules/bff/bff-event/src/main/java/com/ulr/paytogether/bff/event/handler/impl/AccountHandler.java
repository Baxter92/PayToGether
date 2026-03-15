package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.core.event.AccountActivationEvent;
import com.ulr.paytogether.core.event.AccountDeactivationEvent;
import com.ulr.paytogether.core.event.AccountValidationEvent;
import com.ulr.paytogether.core.domaine.service.EmailNotificationService;
import com.ulr.paytogether.core.domaine.service.ValidationTokenService;
import com.ulr.paytogether.core.modele.ValidationTokenModele;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.ulr.paytogether.bff.event.utils.EventUtils.DATE_FORMATTER;

/**
 * Handler pour traiter les événements de validation de compte.
 * Envoie un email avec un token de validation valide 24h.
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
public class AccountHandler implements ConsumerHandler {

    private static final Logger log = LoggerFactory.getLogger(AccountHandler.class);

    private final EmailNotificationService emailNotificationService;
    private final ValidationTokenService validationTokenService;
    private final LocalContainerEntityManagerFactoryBean entityManagerFactory2;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    /**
     * Handler pour traiter l'événement AccountValidationEvent.
     * Envoie un email de validation à l'utilisateur avec un lien contenant un token.
     *
     * ⚠️ Retry automatique : hérité de ConsumerHandler (3 tentatives, backoff exponentiel)
     *
     * @param event L'événement de validation de compte
     */
    @FunctionalHandler(
        eventType = AccountValidationEvent.class,
        maxAttempts = 2,
        description = "Envoie un email de validation de compte avec token 24h"
    )
    public void handleAccountValidation(AccountValidationEvent event) {
        log.info("Handling AccountValidationEvent: utilisateur={}, email={}",
                event.getUtilisateurUuid(), event.getEmail());

        try {
            // 1. Sauvegarder le token via le Service métier
            ValidationTokenModele tokenModele = ValidationTokenModele.builder()
                    .token(event.getToken())
                    .utilisateurUuid(event.getUtilisateurUuid())
                    .dateExpiration(event.getDateExpiration())
                    .typeToken("VALIDATION_COMPTE")
                    .utilise(false)
                    .build();
            validationTokenService.creer(tokenModele);
            log.info("Token de validation sauvegardé pour utilisateur: {}", event.getUtilisateurUuid());

            // 2. Préparer les variables du template
            Map<String, Object> variables = new HashMap<>();
            variables.put("prenom", event.getPrenom());
            variables.put("nom", event.getNom());
            variables.put("token", event.getToken());
            variables.put("lienValidation", construireLienValidation("account-validation", event.getToken()));
            variables.put("dateExpiration", event.getDateExpiration().format(DATE_FORMATTER));

            // 3. Envoyer l'email via le service métier
            emailNotificationService.envoyerNotification(
                    event.getEmail(),
                    "Validating your DealToGether account",
                    "notification-account-validation-en",
                    variables
            );

            log.info("AccountValidationEvent handled successfully for user: {}", event.getUtilisateurUuid());

        } catch (Exception e) {
            log.error("Error handling AccountValidationEvent: {}", e.getMessage(), e);
            throw e; // Propagation pour retry automatique
        }
    }

    @FunctionalHandler(
            eventType = AccountActivationEvent.class,
            maxAttempts = 2,
            description = "Envoie un email de confirmation d'activation de compte"
    )
    public void handleAccountActivation(AccountActivationEvent event) {
        log.info("Handling AccountActivationEvent: utilisateur={}, email={}",
                event.getUtilisateurUuid(), event.getEmail());

        try {
            // Préparer les variables du template
            Map<String, Object> variables = new HashMap<>();
            variables.put("prenom", event.getPrenom());
            variables.put("nom", event.getNom());
            variables.put("dateActivation", event.getDateActivation().format(DATE_FORMATTER));
            variables.put("lienConnexion", construireLienValidation("login", null));

            // Envoyer l'email de confirmation d'activation
            emailNotificationService.envoyerNotification(
                    event.getEmail(),
                    "Your DealToGether account is activated",
                    "notification-account-activation-en",
                    variables
            );

            log.info("AccountActivationEvent handled successfully for user: {}", event.getUtilisateurUuid());

        } catch (Exception e) {
            log.error("Error handling AccountActivationEvent: {}", e.getMessage(), e);
            throw e; // Propagation pour retry automatique
        }
    }

    @FunctionalHandler(
            eventType = AccountDeactivationEvent.class,
            maxAttempts = 2,
            description = "Envoie un email de confirmation de désactivation de compte"
    )
    public void handleAccountDesActivation(AccountDeactivationEvent event) {
        log.info("Handling AccountDeactivationEvent: utilisateur={}, email={}",
                event.getUtilisateurUuid(), event.getEmail());

        try {
            // Préparer les variables du template
            Map<String, Object> variables = new HashMap<>();
            variables.put("prenom", event.getPrenom());
            variables.put("nom", event.getNom());
            variables.put("dateDesactivation", event.getDateDesactivation().format(DATE_FORMATTER));
            variables.put("raisonDesactivation", "Nous sommes désolés de vous voir partir. Si vous avez des questions ou des préoccupations, n'hésitez pas à nous contacter.");

            // Envoyer l'email de confirmation d'activation
            emailNotificationService.envoyerNotification(
                    event.getEmail(),
                    "Your DealToGether account is deactivated",
                    "notification-desactivation-compte-en",
                    variables
            );

            log.info("AccountDeactivationEvent handled successfully for user: {}", event.getUtilisateurUuid());

        } catch (Exception e) {
            log.error("Error handling AccountDeactivationEvent: {}", e.getMessage(), e);
            throw e; // Propagation pour retry automatique
        }
    }

    /**
     * Construit le lien de validation avec le token
     */
    private String construireLienValidation(String path, String token) {
        String lien = frontendBaseUrl + "/%s".formatted(path);
        if (token != null) {
            lien =  lien + "?token=%s".formatted(token);
        }
        return lien;
    }
}

