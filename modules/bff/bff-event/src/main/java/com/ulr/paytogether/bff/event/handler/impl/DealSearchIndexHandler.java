package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.core.domaine.service.DealRechercheService;
import com.ulr.paytogether.core.domaine.service.DealService;
import com.ulr.paytogether.core.event.DealCreatedEvent;
import com.ulr.paytogether.core.event.DealUpdatedEvent;
import com.ulr.paytogether.core.event.DealCancelledEvent;
import com.ulr.paytogether.core.modele.DealModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler pour l'indexation automatique des deals dans Elasticsearch
 * Écoute les événements de création, modification et suppression de deals
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DealSearchIndexHandler implements ConsumerHandler {

    private final DealRechercheService dealRechercheService;
    private final DealService dealService;

    /**
     * Indexe automatiquement un deal lors de sa création
     */
    @FunctionalHandler(
        eventType = DealCreatedEvent.class,
        description = "Indexe automatiquement un deal dans Elasticsearch lors de sa création"
    )
    public void handleDealCreated(DealCreatedEvent event) {
        log.info("Indexation Elasticsearch du deal créé: {}", event.getDealUuid());

        try {
            // Récupérer le deal complet depuis la BDD
            DealModele deal = dealService.lireParUuid(event.getDealUuid())
                    .orElseThrow(() -> new RuntimeException("Deal non trouvé: " + event.getDealUuid()));

            // Indexer dans Elasticsearch
            dealRechercheService.indexerDeal(deal);

            log.info("Deal {} indexé avec succès dans Elasticsearch", event.getDealUuid());
        } catch (Exception e) {
            log.error("⚠️ Erreur lors de l'indexation du deal {} : {} (ignorer si vous êtes en train de réindexer)",
                event.getDealUuid(), e.getMessage());
            // NE PAS lancer d'exception pour ne pas bloquer l'application
        }
    }

    /**
     * Met à jour l'index Elasticsearch lors de la modification d'un deal
     */
    @FunctionalHandler(
        eventType = DealUpdatedEvent.class,
        description = "Met à jour l'index Elasticsearch lors de la modification d'un deal"
    )
    public void handleDealUpdated(DealUpdatedEvent event) {
        log.info("Mise à jour Elasticsearch du deal: {}", event.getDealUuid());

        try {
            // Récupérer le deal complet depuis la BDD
            DealModele deal = dealService.lireParUuid(event.getDealUuid())
                    .orElseThrow(() -> new RuntimeException("Deal non trouvé: " + event.getDealUuid()));

            // Mettre à jour dans Elasticsearch
            dealRechercheService.mettreAJourIndexDeal(deal);

            log.info("Index Elasticsearch du deal {} mis à jour avec succès", event.getDealUuid());
        } catch (Exception e) {
            log.error("⚠️ Erreur lors de la mise à jour de l'index du deal {} : {} (ignorer si vous êtes en train de réindexer)",
                event.getDealUuid(), e.getMessage());
            // NE PAS lancer d'exception pour ne pas bloquer l'application
        }
    }

    /**
     * Supprime le deal de l'index Elasticsearch lors de son annulation
     */
    @FunctionalHandler(
        eventType = DealCancelledEvent.class,
        description = "Supprime le deal de l'index Elasticsearch lors de son annulation"
    )
    public void handleDealCancelled(DealCancelledEvent event) {
        log.info("Suppression Elasticsearch du deal annulé: {}", event.getDealUuid());

        try {
            // Supprimer de l'index Elasticsearch
            dealRechercheService.supprimerIndexDeal(event.getDealUuid());

            log.info("Deal {} supprimé de l'index Elasticsearch avec succès", event.getDealUuid());
        } catch (Exception e) {
            log.error("⚠️ Erreur lors de la suppression de l'index du deal {} : {} (ignorer si vous êtes en train de réindexer)",
                event.getDealUuid(), e.getMessage());
            // NE PAS lancer d'exception pour ne pas bloquer l'application
        }
    }
}

