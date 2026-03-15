package com.ulr.paytogether.configuration.test.config;

import com.ulr.paytogether.core.domaine.service.DealRechercheService;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.DealRechercheModele;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.UUID;

/**
 * Configuration de test pour fournir un mock de DealRechercheService
 * Car Elasticsearch n'est pas disponible en tests
 */
@TestConfiguration
public class TestConfig {

    /**
     * Bean mock de DealRechercheService pour les tests
     */
    @Bean
    @Primary
    public DealRechercheService dealRechercheService() {
        return new DealRechercheService() {
            @Override
            public List<DealRechercheModele> rechercherDeals(String query) {
                // Retourne une liste vide pour les tests
                return List.of();
            }

            @Override
            public void indexerDeal(DealModele deal) {
                // Ne fait rien en tests
            }

            @Override
            public void mettreAJourIndexDeal(DealModele deal) {
                // Ne fait rien en tests
            }

            @Override
            public void supprimerIndexDeal(UUID dealUuid) {
                // Ne fait rien en tests
            }

            @Override
            public void reindexerTousLesDeals() {
                // Ne fait rien en tests
            }
        };
    }
}

