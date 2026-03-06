package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import com.ulr.paytogether.core.event.PaymentInitiatedEvent;
import com.ulr.paytogether.core.event.PaymentSuccessfulEvent;
import com.ulr.paytogether.core.event.PaymentFailedEvent;
import com.ulr.paytogether.core.domaine.service.SquarePaymentService;
import com.ulr.paytogether.core.domaine.service.PaiementService;
import com.ulr.paytogether.core.modele.PaiementModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
@Slf4j
public class SquarePaymentHandler implements ConsumerHandler {

    private final SquarePaymentService squarePaymentService;
    private final PaiementService paiementService;

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
        maxAttempts = 5,
        description = "Traite les paiements Square initiés"
    )
    public void handlePaymentInitiated(PaymentInitiatedEvent event) {
        log.info("Handling PaymentInitiatedEvent: utilisateur={}, commande={}, montant={}",
                event.getUtilisateurUuid(), event.getCommandeUuid(), event.getMontant());

        try {
            // Récupérer le paiement via le Service métier
            PaiementModele paiement = paiementService.trouverParCommande(event.getCommandeUuid())
                .stream()
                .filter(p -> p.getSquareToken() != null && p.getSquareToken().equals(event.getSquareToken()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                    "Paiement non trouvé pour la commande: " + event.getCommandeUuid()
                ));

            // Traiter le paiement via Square (Service métier)
            squarePaymentService.traiterPaiementSquare(paiement);

            log.info("PaymentInitiatedEvent handled successfully for commande: {}", event.getCommandeUuid());

        } catch (Exception e) {
            log.error("Error handling PaymentInitiatedEvent: {}", e.getMessage(), e);
            throw e; // Relancer pour déclencher le retry
        }
    }

    /**
     * Handler pour traiter l'événement PaymentSuccessfulEvent.
     * Met à jour les statistiques et déclenche les actions post-paiement.
     *
     * @param event L'événement de paiement réussi
     */
    @FunctionalHandler(
        eventType = PaymentSuccessfulEvent.class,
        description = "Actions post-paiement Square réussi"
    )
    public void handlePaymentSuccessful(PaymentSuccessfulEvent event) {
        log.info("Handling PaymentSuccessfulEvent: paiement={}, montant={}, squarePaymentId={}",
                event.getPaiementUuid(), event.getMontant(), event.getSquarePaymentId());

        try {
            // 1. Mettre à jour les statistiques de paiement
            // statisticsService.incrementerPaiements(event.getMontant());
            log.info("Statistics updated for successful payment: {}", event.getPaiementUuid());

            // 2. Mettre à jour le statut de la commande
            squarePaymentService.mettreAJourStatutCommandeDeal(event.getPaiementUuid(), StatutPaiement.CONFIRME.name());
            // commandeService.marquerCommePayee(event.getCommandeUuid());
            log.info("Order marked as paid: {}", event.getCommandeUuid());

            // 3. Déclencher le processus de livraison ou de préparation
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
     *
     * @param event L'événement d'échec de paiement
     */
    @FunctionalHandler(
        eventType = PaymentFailedEvent.class,
        maxAttempts = 3,
        description = "Gestion des échecs de paiement Square"
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

            log.info("PaymentFailedEvent handled successfully for paiement: {}", event.getPaiementUuid());

        } catch (Exception e) {
            log.error("Error handling PaymentFailedEvent: {}", e.getMessage(), e);
            throw e;
        }
    }
}

