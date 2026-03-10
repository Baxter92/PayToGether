package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.DealRechercheModele;
import com.ulr.paytogether.core.provider.DealRechercheProvider;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.provider.adapter.entity.elasticsearch.DealDocument;
import com.ulr.paytogether.provider.adapter.mapper.DealSearchMapper;
import com.ulr.paytogether.provider.repository.DealRepository;
import com.ulr.paytogether.provider.repository.elasticsearch.DealSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur pour la recherche Elasticsearch de deals
 * Implémente le port DealRechercheProvider (bff-core)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DealRechercheProviderAdapter implements DealRechercheProvider {

    private final DealSearchRepository searchRepository;
    private final DealRepository dealRepository;
    private final DealSearchMapper searchMapper;

    @Override
    public List<DealRechercheModele> rechercherDeals(String query) {
        log.debug("Recherche Elasticsearch avec query: {}", query);

        // Recherche dans plusieurs champs : titre, description, ville, catégorie
        List<DealDocument> documents = searchRepository
                .findByTitreContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrVilleContainingIgnoreCaseOrCategorieNomContainingIgnoreCase(
                        query, query, query, query);

        // Filtrer uniquement les deals publiés (statut stocké en String)
        List<DealRechercheModele> resultats = documents.stream()
                .filter(doc -> StatutDeal.PUBLIE.name().equals(doc.getStatut()))
                .map(searchMapper::versModeleRecherche)
                .collect(Collectors.toList());

        log.debug("Trouvé {} deal(s) publiés", resultats.size());
        return resultats;
    }

    @Override
    public void indexerDeal(DealModele deal) {
        log.debug("Indexation du deal: {}", deal.getUuid());

        DealDocument document = searchMapper.versDocument(deal);
        searchRepository.save(document);

        log.info("Deal {} indexé avec succès", deal.getUuid());
    }

    @Override
    public void mettreAJourIndexDeal(DealModele deal) {
        log.debug("Mise à jour de l'index pour le deal: {}", deal.getUuid());

        // Suppression puis réindexation
        searchRepository.deleteById(deal.getUuid().toString());

        DealDocument document = searchMapper.versDocument(deal);
        searchRepository.save(document);

        log.info("Index du deal {} mis à jour avec succès", deal.getUuid());
    }

    @Override
    public void supprimerIndexDeal(UUID uuid) {
        log.debug("Suppression de l'index pour le deal: {}", uuid);

        searchRepository.deleteById(uuid.toString());

        log.info("Index du deal {} supprimé avec succès", uuid);
    }

    @Override
    public void reindexerTousLesDeals() {
        log.info("Début de la réindexation de tous les deals");

        // Supprimer tous les documents existants
        searchRepository.deleteAll();
        log.info("Index nettoyé");

        // Récupérer tous les deals de la BDD
        List<DealJpa> allDeals = dealRepository.findAll();
        log.info("Trouvé {} deals à réindexer", allDeals.size());

        // Convertir et sauvegarder dans Elasticsearch
        List<DealDocument> documents = allDeals.stream()
                .map(searchMapper::versDocument)
                .collect(Collectors.toList());

        searchRepository.saveAll(documents);

        log.info("Réindexation terminée : {} deals indexés", documents.size());
    }
}

