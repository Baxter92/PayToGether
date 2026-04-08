package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.core.event.PayoutValidatedEvent;
import com.ulr.paytogether.core.domaine.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler pour traiter l'événement de validation de payout
 * Envoie un mail au vendeur pour lui demander d'uploader sa facture
 *
 * ⚠️ ARCHITECTURE HEXAGONALE :
 * - Ce handler fait partie de la PARTIE GAUCHE (bff-event - adaptateur d'entrée)
 * - Il appelle uniquement les SERVICES du CORE (bff-core)
 * - Il ne doit JAMAIS accéder directement aux Providers ou Repositories
 */
@Component
@RequiredArgsConstructor
public class PayoutValidatedHandler implements ConsumerHandler {
    
    private static final Logger log = LoggerFactory.getLogger(PayoutValidatedHandler.class);

    private final EmailNotificationService emailNotificationService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Traite l'événement de validation de payout
     * Envoie un email au vendeur pour lui demander d'uploader sa facture
     *
     * @param event Événement de validation de payout
     */
    @FunctionalHandler(
        eventType = PayoutValidatedEvent.class,
        maxAttempts = 3,
        description = "Envoie un email au vendeur pour demander l'upload de sa facture"
    )
    public void handlePayoutValidated(PayoutValidatedEvent event) {
        log.info("Traitement de l'événement PayoutValidatedEvent pour la commande: {}", event.getCommandeUuid());
        
        try {
            // Envoyer l'email au vendeur pour lui demander d'uploader sa facture
            // L'email est la seule opération, donc on l'isole pour éviter les doublons
            envoyerEmailDemandFacture(event);

        } catch (Exception e) {
            log.error("Erreur lors du traitement de PayoutValidatedEvent pour la commande {}: {}",
                event.getCommandeUuid(), e.getMessage(), e);
            // Ne pas propager l'exception pour éviter les doublons d'emails en cas de retry
        }
    }

    /**
     * Envoie l'email de demande de facture au vendeur.
     * En cas d'échec, l'exception n'est PAS propagée pour éviter un retry complet.
     */
    private void envoyerEmailDemandFacture(PayoutValidatedEvent event) {
        try {
            // Construire l'URL pour uploader la facture
            String uploadUrl = frontendBaseUrl + "/vendeur/commandes/" + event.getCommandeUuid() + "/facture";
            
            // Préparer les paramètres du template email
            Map<String, Object> templateParams = new HashMap<>();
            templateParams.put("vendeurNom", event.getNomVendeur());
            templateParams.put("numeroCommande", event.getNumeroCommande());
            templateParams.put("dateDepotPayout", event.getDateDepotPayout().format(DATE_FORMATTER));
            templateParams.put("uploadUrl", uploadUrl);
            
            // Envoyer l'email
            emailNotificationService.envoyerEmailAvecTemplate(
                event.getEmailVendeur(),
                "Payout Validated - Please Upload Your Invoice",
                "payout-validated", // Template Thymeleaf
                templateParams
            );
            
            log.info("✅ Email de demande de facture envoyé au vendeur {} pour la commande {}", 
                event.getEmailVendeur(), event.getNumeroCommande());
        } catch (Exception e) {
            log.error("⚠️ Échec de l'envoi de l'email de payout au vendeur {} pour la commande {}: {}", 
                event.getEmailVendeur(), event.getNumeroCommande(), e.getMessage(), e);
        }
    }
}

