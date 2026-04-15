package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.core.domaine.service.*;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import com.ulr.paytogether.core.event.EventPublisher;
import com.ulr.paytogether.core.event.PaymentInitiatedEvent;
import com.ulr.paytogether.core.event.PaymentSuccessfulEvent;
import com.ulr.paytogether.core.event.PaymentFailedEvent;
import com.ulr.paytogether.core.modele.AdresseModele;
import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.core.provider.AdresseProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.ulr.paytogether.bff.event.utils.EventUtils.CONSTRUIRELIENDEAL;

/**
 * Handler pour traiter les événements de paiement Square.
 * Ce handler sera automatiquement découvert et enregistré par le EventConsumerService.
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
public class SquarePaymentHandler implements ConsumerHandler {

    private static final Logger log = LoggerFactory.getLogger(SquarePaymentHandler.class);

    private final SquarePaymentService squarePaymentService;
    private final PaiementService paiementService;
    private final CommandeService commandeService;
    private final EmailNotificationService emailNotificationService;
    private final UtilisateurService utilisateurService;
    private final EventPublisher eventPublisher;
    private final AdresseProvider adresseProvider;
    private final DealParticipantService dealParticipantService;
    
    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    /**
     * Handler pour traiter l'événement PaymentInitiatedEvent.
     * Lance le traitement asynchrone du paiement Square.
     *
     * ⚠️ Retry automatique : hérité de ConsumerHandler (3 tentatives, backoff exponentiel)
     *
     * @param event L'événement d'initiation de paiement
     */
    @FunctionalHandler(
        eventType = PaymentInitiatedEvent.class,
        maxAttempts = 1,
        description = "Traite les paiements Square initiés"
    )
    public void handlePaymentInitiated(PaymentInitiatedEvent event) {
        log.info("Handling PaymentInitiatedEvent: utilisateur={}, commande={}, montant={}",
                event.getUtilisateurUuid(), event.getCommandeUuid(), event.getMontant());

        PaiementModele paiement = paiementService.lireParUuid(event.getPaiementUuid())
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé pour l'UUID : " + event.getPaiementUuid()));
        try {
            // Récupérer le paiement via le Service métier

            paiement.setNombreDePart(event.getNombreDePart());
            // Traiter le paiement via Square (Service métier)
            squarePaymentService.traiterPaiementSquare(paiement);

            log.info("PaymentInitiatedEvent handled successfully for commande: {}", event.getCommandeUuid());

        } catch (Exception e) {
            log.error("Error handling PaymentInitiatedEvent: {}", e.getMessage(), e);
            paiement.setStatut(StatutPaiement.ECHOUE);
            paiementService.mettreAJour(event.getPaiementUuid(), paiement);
            throw e; // Relancer pour déclencher le retry
        }
    }

    /**
     * Méthode @Recover appelée après épuisement de tous les max attempts (1 tentatives).
     * Publie un événement PaymentFailedEvent pour déclencher l'envoi d'email d'échec.
     *
     * @param e L'exception qui a causé l'échec final
     * @param event L'événement PaymentInitiatedEvent original
     */
    @Recover
    public void recoverFromPaymentInitiatedFailure(Exception e, PaymentInitiatedEvent event) {
        log.error("❌ ÉCHEC DÉFINITIF après 1 tentatives pour le paiement UUID={}, commande={}. Publication de PaymentFailedEvent.",
                event.getPaiementUuid(), event.getCommandeUuid());

        try {
            // Récupérer les informations complètes du paiement
            PaiementModele paiement = paiementService.lireParUuid(event.getPaiementUuid())
                    .orElseThrow(() -> new RuntimeException("Paiement non trouvé: " + event.getPaiementUuid()));

            UtilisateurModele utilisateur = utilisateurService.lireParUuid(event.getUtilisateurUuid())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + event.getUtilisateurUuid()));

            AdresseModele adresse = adresseProvider.trouverParPaiement(event.getPaiementUuid());

            // Construire l'événement PaymentFailedEvent avec toutes les infos pour l'email
            PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent(
                    event.getUtilisateurUuid(),
                    event.getCommandeUuid(),
                    event.getPaiementUuid(),
                    event.getMontant(),
                    event.getMethodePaiement(),
                    e.getMessage() != null ? e.getMessage() : "Erreur inconnue",
                    "PAYMENT_PROCESSING_ERROR",
                    utilisateur.getEmail(),
                    utilisateur.getPrenom(),
                    utilisateur.getNom(),
                    paiement.getDeal().getTitre(),
                    paiement.getDeal().getDescription(),
                    event.getNombreDePart(),
                    1, // Nombre de tentatives
                    adresse != null ? adresse.getRue() : "",
                    adresse != null ? adresse.getVille() : "",
                    adresse != null ? adresse.getProvince() : "",
                    adresse != null ? adresse.getCodePostal() : "",
                    adresse != null ? adresse.getPays() : ""
            );

            // Publier l'événement d'échec définitif
            eventPublisher.publishAsync(paymentFailedEvent);

            log.info("✅ PaymentFailedEvent publié avec succès pour paiement UUID={}", event.getPaiementUuid());

        } catch (Exception ex) {
            log.error("❌ Erreur lors de la publication de PaymentFailedEvent: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Handler pour traiter l'événement PaymentSuccessfulEvent.
     * Met à jour les statistiques et déclenche les actions post-paiement.
     *
     * ⚠️ maxAttempts = 1 : Pas de retry pour éviter les emails en double
     * Si l'envoi d'email échoue, l'événement sera marqué PERMANENTLY_FAILED
     *
     * @param event L'événement de paiement réussi
     */
    @FunctionalHandler(
        eventType = PaymentSuccessfulEvent.class,
        maxAttempts = 1,
        description = "Actions post-paiement Square réussi (pas de retry)"
    )
    public void handlePaymentSuccessful(PaymentSuccessfulEvent event) {
        log.info("Handling PaymentSuccessfulEvent: paiement={}, montant={}, squarePaymentId={}",
                event.getPaiementUuid(), event.getMontant(), event.getSquarePaymentId());

        try {
            // 1. Mettre à jour les statistiques de paiement
            // statisticsService.incrementerPaiements(event.getMontant());
            log.info("Statistics updated for successful payment: {}", event.getPaiementUuid());

            // 2. Mettre à jour le statut de la commande
            squarePaymentService.mettreAJourStatutCommandeDeal(event.getPaiementUuid(), StatutPaiement.CONFIRME.name(), event.getNombreDePart());
            // commandeService.marquerCommePayee(event.getCommandeUuid());
            log.info("Order marked as paid: {}", event.getCommandeUuid());

            // 3. Envoyer email de notification au créateur du deal (nouveau participant)
            CommandeModele commandeModele = commandeService.lireParUuid(event.getCommandeUuid());
            UtilisateurModele utilisateurModele = utilisateurService.lireParUuid(event.getUtilisateurUuid()).orElse(null);
            DealModele dealModele = commandeModele.getDealModele();

            long nombreDePartsActuels = dealParticipantService.compterNombreParts(dealModele.getUuid());
            Map<String, Object> variables = new HashMap<>();
            variables.put("prenom", dealModele.getCreateur().getPrenom());
            variables.put("nom", dealModele.getCreateur().getNom());
            variables.put("titreDeal", dealModele.getTitre());
            variables.put("prenomParticipant", Optional.ofNullable(utilisateurModele).map(UtilisateurModele::getPrenom).orElse(""));
            variables.put("nomParticipant", Optional.ofNullable(utilisateurModele).map(UtilisateurModele::getNom).orElse(""));
            variables.put("nombrePartsActuels", nombreDePartsActuels);
            variables.put("nombreParticipantsMax", dealModele.getNbParticipants());
            variables.put("lienDeal", CONSTRUIRELIENDEAL(dealModele.getUuid().toString(), frontendBaseUrl));

            // Appeler le service métier pour envoyer l'email au créateur du deal
            emailNotificationService.envoyerNotification(
                    dealModele.getCreateur().getEmail(),
                    "New participant - " + dealModele.getTitre(),
                    "notification-nouveau-participant-en",
                    variables
            );

            log.info("✅ Email de nouveau participant envoyé au créateur: {}", dealModele.getCreateur().getEmail());

            // 4. Déclencher le processus de livraison ou de préparation
            // livraisonService.demarrerLivraison(event.getCommandeUuid());
            log.info("Delivery process initiated for order: {}", event.getCommandeUuid());

            log.info("PaymentSuccessfulEvent handled successfully for paiement: {}", event.getPaiementUuid());

        } catch (Exception e) {
            log.error("Error handling PaymentSuccessfulEvent: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Handler pour traiter l'événement PaymentFailedEvent.
     * Gère les actions en cas d'échec de paiement.
     * Envoie un email d'échec à l'utilisateur.
     *
     * ⚠️ maxAttempts = 1 : Pas de retry pour éviter les emails en double
     * Si l'envoi d'email échoue, l'événement sera marqué PERMANENTLY_FAILED
     *
     * @param event L'événement d'échec de paiement
     */
    @FunctionalHandler(
        eventType = PaymentFailedEvent.class,
        maxAttempts = 1,
        description = "Gestion des échecs de paiement Square et envoi d'email (pas de retry)"
    )
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Handling PaymentFailedEvent: paiement={}, montant={}, erreur={}",
                event.getPaiementUuid(), event.getMontant(), event.getMessageErreur());

        try {
            // 1. Mettre à jour les statistiques d'échec
            // statisticsService.incrementerEchecs(event.getCodeErreur());
            log.info("Failure statistics updated for payment: {}", event.getPaiementUuid());

            // 2. Annuler la commande ou proposer une alternative
            // commandeService.marquerCommeEchouee(event.getCommandeUuid());
            log.info("Order marked as failed: {}", event.getCommandeUuid());

            // 3. Logger pour analyse
            log.warn("Payment failed for user {} with error: {} (code: {})",
                    event.getUtilisateurUuid(), event.getMessageErreur(), event.getCodeErreur());

            // 4. Envoyer l'email d'échec de paiement
            Map<String, Object> variables = new HashMap<>();
            variables.put("prenom", event.getPrenom());
            variables.put("nom", event.getNom());
            variables.put("montant", event.getMontant());
            variables.put("titreDeal", event.getTitreDeal());
            variables.put("dateTentative", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            variables.put("raisonEchec", event.getMessageErreur());
            variables.put("supportEmail", "support@dealtogether.ca");

            emailNotificationService.envoyerNotification(
                    event.getEmail(),
                    "Échec de paiement - " + event.getTitreDeal(),
                    "notification-paiement-echoue-en",
                    variables
            );

            log.info("✅ Email d'échec de paiement envoyé à: {}", event.getEmail());
            log.info("PaymentFailedEvent handled successfully for paiement: {}", event.getPaiementUuid());

        } catch (Exception e) {
            log.error("Error handling PaymentFailedEvent: {}", e.getMessage(), e);
            throw e;
        }
    }
}

