package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.provider.adapter.entity.elasticsearch.DealDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Contrôleur REST pour l'administration des index Elasticsearch
 * Endpoints réservés aux administrateurs
 */
@RestController
@RequestMapping("/api/admin/elasticsearch")
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchAdminResource {

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * Lister tous les index Elasticsearch connus
     *
     * @return Liste des index
     */
    @GetMapping("/indices")
    public ResponseEntity<Map<String, Object>> listerIndex() {
        log.info("Demande de liste des index Elasticsearch");

        try {
            // Liste des index connus dans l'application
            List<String> knownIndices = Collections.singletonList("deals");
            List<Map<String, Object>> indicesInfo = new ArrayList<>();

            for (String indexName : knownIndices) {
                try {
                    IndexOperations indexOps = elasticsearchOperations.indexOps(
                        DealDocument.class
                    );

                    boolean exists = indexOps.exists();

                    Map<String, Object> info = new HashMap<>();
                    info.put("name", indexName);
                    info.put("exists", exists);

                    indicesInfo.add(info);
                } catch (Exception e) {
                    log.warn("Erreur lors de la vérification de l'index {} : {}", indexName, e.getMessage());

                    Map<String, Object> info = new HashMap<>();
                    info.put("name", indexName);
                    info.put("exists", false);
                    info.put("error", e.getMessage());

                    indicesInfo.add(info);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("count", indicesInfo.size());
            response.put("indices", indicesInfo);

            log.info("Retour de {} index", indicesInfo.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des index : {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Impossible de récupérer les index");
            error.put("message", e.getMessage());

            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Supprimer l'index 'deals'
     *
     * @return Réponse de succès ou d'erreur
     */
    @DeleteMapping("/indices/deals")
    public ResponseEntity<Map<String, Object>> supprimerIndexDeals() {
        log.warn("⚠️ Demande de suppression de l'index 'deals'");

        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(
                DealDocument.class
            );

            boolean exists = indexOps.exists();

            if (!exists) {
                log.info("L'index 'deals' n'existe pas déjà");

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "L'index 'deals' n'existe pas (déjà supprimé ou jamais créé)");
                response.put("exists", false);

                return ResponseEntity.ok(response);
            }

            // Supprimer l'index (forcer la suppression)
            boolean deleted = indexOps.delete();

            // Attendre un peu pour s'assurer que la suppression est propagée
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Vérifier que l'index est bien supprimé
            boolean stillExists = indexOps.exists();

            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted && !stillExists);
            response.put("deleted", deleted);
            response.put("existsAfterDeletion", stillExists);
            response.put("message", deleted && !stillExists
                ? "Index 'deals' supprimé avec succès et vérifié"
                : deleted
                    ? "Index 'deals' supprimé mais existe toujours (attendre quelques secondes)"
                    : "Échec de la suppression de l'index 'deals'");

            log.warn("Index 'deals' supprimé : {}, existe encore : {}", deleted, stillExists);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'index 'deals' : {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Impossible de supprimer l'index");
            error.put("message", e.getMessage());
            error.put("type", e.getClass().getSimpleName());

            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Vérifier si l'index 'deals' existe
     *
     * @return Statut de l'index
     */
    @GetMapping("/indices/deals/exists")
    public ResponseEntity<Map<String, Object>> verifierIndexDeals() {
        log.debug("Vérification de l'existence de l'index 'deals'");

        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(
                DealDocument.class
            );

            boolean exists = indexOps.exists();

            Map<String, Object> response = new HashMap<>();
            response.put("index", "deals");
            response.put("exists", exists);
            response.put("message", exists ? "L'index 'deals' existe" : "L'index 'deals' n'existe pas");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la vérification de l'index 'deals' : {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("index", "deals");
            error.put("exists", false);
            error.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Vérifier la connexion à Elasticsearch
     *
     * @return Statut de la connexion
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> verifierConnexion() {
        log.debug("Vérification de la connexion Elasticsearch");

        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(
                com.ulr.paytogether.provider.adapter.entity.elasticsearch.DealDocument.class
            );

            // Tester la connexion en vérifiant si on peut interroger l'index
            indexOps.exists();

            Map<String, Object> response = new HashMap<>();
            response.put("connected", true);
            response.put("status", "UP");
            response.put("message", "Connexion Elasticsearch OK");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur de connexion Elasticsearch : {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("connected", false);
            error.put("status", "DOWN");
            error.put("message", e.getMessage());

            return ResponseEntity.status(503).body(error);
        }
    }
}

