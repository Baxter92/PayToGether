package com.ulr.paytogether.provider.repository.elasticsearch;

import com.ulr.paytogether.provider.adapter.entity.elasticsearch.DealDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository Elasticsearch pour la recherche de deals
 */
@Repository
public interface DealSearchRepository extends ElasticsearchRepository<DealDocument, String> {

    /**
     * Recherche de deals par UUID
     * @param uuid UUID du deal
     * @return Document deal trouvé
     */
    DealDocument findByUuid(UUID uuid);

    /**
     * Recherche full-text dans les deals (titre, description, ville, catégorie)
     * @param query Texte de recherche
     * @return Liste de deals correspondants
     */
    List<DealDocument> findByTitreContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrVilleContainingIgnoreCaseOrCategorieNomContainingIgnoreCase(
            String titre, String description, String ville, String categorieNom);
}

