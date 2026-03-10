package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.DealRechercheApiAdapter;
import com.ulr.paytogether.api.dto.DealRechercheDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la recherche de deals
 * Endpoint public : /api/recherche/deals
 */
@RestController
@RequestMapping("/api/recherche")
@RequiredArgsConstructor
@Slf4j
public class DealRechercheResource {

    private final DealRechercheApiAdapter apiAdapter;

    /**
     * Recherche globale de deals par texte
     * Endpoint public accessible sans authentification
     *
     * @param query Texte de recherche (query parameter)
     * @return Liste de deals correspondants
     *
     * Exemple: GET /api/recherche/deals?q=pizza
     */
    @GetMapping("/deals")
    public ResponseEntity<List<DealRechercheDTO>> rechercherDeals(
            @RequestParam(value = "q", required = false, defaultValue = "") String query) {

        log.info("Recherche de deals avec query: {}", query);

        if (query.trim().isEmpty()) {
            log.warn("Query de recherche vide");
            return ResponseEntity.ok(List.of());
        }

        List<DealRechercheDTO> resultats = apiAdapter.rechercherDeals(query);

        log.info("Retour de {} résultat(s)", resultats.size());

        return ResponseEntity.ok(resultats);
    }

    /**
     * Réindexer tous les deals dans Elasticsearch
     * Endpoint réservé aux administrateurs
     *
     * @return Réponse de succès
     */
    @PostMapping("/deals/reindex")
    public ResponseEntity<String> reindexerTousLesDeals() {
        log.info("Demande de réindexation de tous les deals");

        apiAdapter.reindexerTousLesDeals();

        log.info("Réindexation terminée");

        return ResponseEntity.ok("Réindexation terminée avec succès");
    }
}

