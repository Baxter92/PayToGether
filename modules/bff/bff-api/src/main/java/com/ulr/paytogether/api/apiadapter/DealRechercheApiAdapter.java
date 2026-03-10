package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.DealRechercheDTO;
import com.ulr.paytogether.api.mapper.DealRechercheMapper;
import com.ulr.paytogether.core.domaine.service.DealRechercheService;
import com.ulr.paytogether.core.modele.DealRechercheModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptateur API pour la recherche de deals
 * Couche d'adaptation entre l'API REST et le service métier
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DealRechercheApiAdapter {

    private final DealRechercheService dealRechercheService;
    private final DealRechercheMapper mapper;

    /**
     * Recherche globale de deals par texte
     * @param query Texte de recherche
     * @return Liste de deals correspondants
     */
    public List<DealRechercheDTO> rechercherDeals(String query) {
        log.info("Recherche de deals avec query: {}", query);

        List<DealRechercheModele> resultats = dealRechercheService.rechercherDeals(query);

        log.info("Retour de {} résultat(s) pour la recherche: {}", resultats.size(), query);

        return mapper.modelesVersDto(resultats);
    }

    /**
     * Réindexer tous les deals
     */
    public void reindexerTousLesDeals() {
        log.info("Demande de réindexation de tous les deals");
        dealRechercheService.reindexerTousLesDeals();
    }
}

