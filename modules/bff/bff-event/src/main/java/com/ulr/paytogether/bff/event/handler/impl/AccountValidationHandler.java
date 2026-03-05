package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.core.event.AccountValidationEvent;
import com.ulr.paytogether.core.domaine.service.EmailNotificationService;
import com.ulr.paytogether.core.domaine.service.ValidationTokenService;
import com.ulr.paytogether.core.modele.ValidationTokenModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

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
@Slf4j
public class AccountValidationHandler implements ConsumerHandler {

    private final EmailNotificationService emailNotificationService;
    private final ValidationTokenService validationTokenService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

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
        maxAttempts = 3,
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
            variables.put("lienValidation", construireLienValidation(event.getToken()));
            variables.put("dateExpiration", event.getDateExpiration().format(DATE_FORMATTER));

            // 3. Envoyer l'email via le service métier
            emailNotificationService.envoyerNotification(
                    event.getEmail(),
                    "Validation de votre compte PayToGether",
                    "notification-account-validation",
                    variables
            );

            log.info("AccountValidationEvent handled successfully for user: {}", event.getUtilisateurUuid());

        } catch (Exception e) {
            log.error("Error handling AccountValidationEvent: {}", e.getMessage(), e);
            throw e; // Propagation pour retry automatique
        }
    }

    /**
     * Construit le lien de validation avec le token
     */
    private String construireLienValidation(String token) {
        // TODO: Récupérer l'URL depuis la configuration
        return "https://dev.dealtogether.ca/validation-compte?token=" + token;
    }
}

