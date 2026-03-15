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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
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
@ConditionalOnProperty(name = "spring.data.elasticsearch.repositories.enabled", havingValue = "true", matchIfMissing = true)
public class DealRechercheProviderAdapter implements DealRechercheProvider {

    private final DealSearchRepository searchRepository;
    private final DealRepository dealRepository;
    private final DealSearchMapper searchMapper;

    @Override
    public List<DealRechercheModele> rechercherDeals(String query) {
        log.debug("Recherche Elasticsearch avec query: {}", query);

        // Recherche dans plusieurs champs : titre, description, ville, catégorie
        List<DealDocument> documents = searchRepository
                .findByTitreContainingIgnoreCaseOrCategorieNomContainingIgnoreCaseOrCreateurNomContainingIgnoreCase(
                        query, query, query);

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

        try {
            // Suppression puis réindexation
            searchRepository.deleteById(deal.getUuid());

            DealDocument document = searchMapper.versDocument(deal);
            searchRepository.save(document);

            log.info("Index du deal {} mis à jour avec succès", deal.getUuid());
        } catch (Exception e) {
            log.warn("Erreur lors de la mise à jour de l'index pour le deal {} : {}", deal.getUuid(), e.getMessage());

            // Si l'index n'existe pas, on indexe simplement le deal
            try {
                DealDocument document = searchMapper.versDocument(deal);
                searchRepository.save(document);
                log.info("Deal {} indexé avec succès (index n'existait pas)", deal.getUuid());
            } catch (Exception ex) {
                log.error("Impossible d'indexer le deal {} : {}", deal.getUuid(), ex.getMessage(), ex);
                throw new RuntimeException("Échec de l'indexation du deal", ex);
            }
        }
    }

    @Override
    public void supprimerIndexDeal(UUID uuid) {
        log.debug("Suppression de l'index pour le deal: {}", uuid);

        try {
            searchRepository.deleteById(uuid);
            log.info("Index du deal {} supprimé avec succès", uuid);
        } catch (Exception e) {
            log.warn("Impossible de supprimer l'index du deal {} (probablement inexistant) : {}", uuid, e.getMessage());
        }
    }

    @Override
    public void reindexerTousLesDeals() {
        log.info("Début de la réindexation de tous les deals");

        // Supprimer tous les documents existants (si l'index existe)
        try {
            searchRepository.deleteAll();
            log.info("Index nettoyé");
        } catch (Exception e) {
            log.warn("Impossible de nettoyer l'index (probablement inexistant) : {}", e.getMessage());
            log.info("L'index sera créé automatiquement lors de la première indexation");
        }

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

