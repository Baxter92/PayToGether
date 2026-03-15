package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.DealRechercheService;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.DealRechercheModele;
import com.ulr.paytogether.core.provider.DealRechercheProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implémentation du service de recherche de deals
 * Contient la logique métier pour la recherche Elasticsearch
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.data.elasticsearch.repositories.enabled", havingValue = "true", matchIfMissing = true)
public class DealRechercheServiceImpl implements DealRechercheService {

    private final DealRechercheProvider dealRechercheProvider;

    @Override
    public List<DealRechercheModele> rechercherDeals(String query) {
        log.info("Recherche de deals avec query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            log.warn("Query de recherche vide, retour d'une liste vide");
            return List.of();
        }

        List<DealRechercheModele> resultats = dealRechercheProvider.rechercherDeals(query.trim());
        log.info("Trouvé {} deal(s) pour la query: {}", resultats.size(), query);

        return resultats;
    }

    @Override
    public void indexerDeal(DealModele deal) {
        log.info("Indexation du deal: {}", deal.getUuid());
        dealRechercheProvider.indexerDeal(deal);
    }

    @Override
    public void mettreAJourIndexDeal(DealModele deal) {
        log.info("Mise à jour de l'index pour le deal: {}", deal.getUuid());
        dealRechercheProvider.mettreAJourIndexDeal(deal);
    }

    @Override
    public void supprimerIndexDeal(UUID uuid) {
        log.info("Suppression de l'index pour le deal: {}", uuid);
        dealRechercheProvider.supprimerIndexDeal(uuid);
    }

    @Override
    public void reindexerTousLesDeals() {
        log.info("Réindexation de tous les deals");
        dealRechercheProvider.reindexerTousLesDeals();
    }
}

