package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.DealRechercheModele;

import java.util.List;
import java.util.UUID;

/**
 * Interface du Provider pour la recherche Elasticsearch de deals
 * Port de sortie (partie droite de l'architecture hexagonale)
 */
public interface DealRechercheProvider {

    /**
     * Recherche globale de deals par texte
     * @param query Texte de recherche
     * @return Liste de deals correspondants
     */
    List<DealRechercheModele> rechercherDeals(String query);

    /**
     * Indexer un deal dans Elasticsearch
     * @param deal Deal à indexer
     */
    void indexerDeal(DealModele deal);

    /**
     * Mettre à jour l'index d'un deal
     * @param deal Deal à mettre à jour
     */
    void mettreAJourIndexDeal(DealModele deal);

    /**
     * Supprimer un deal de l'index
     * @param uuid UUID du deal à supprimer
     */
    void supprimerIndexDeal(UUID uuid);

    /**
     * Réindexer tous les deals
     */
    void reindexerTousLesDeals();
}

