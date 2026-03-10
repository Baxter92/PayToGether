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
public interface DealSearchRepository extends ElasticsearchRepository<DealDocument, UUID> {

    /**
     * Recherche full-text dans les deals (titre, description, ville, catégorie)
     * @param titre Titre à rechercher
     * @param description Description à rechercher
     * @param ville Ville à rechercher
     * @param categorieNom Catégorie à rechercher
     * @return Liste de deals correspondants
     */
    List<DealDocument> findByTitreContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrVilleContainingIgnoreCaseOrCategorieNomContainingIgnoreCase(
            String titre, String description, String ville, String categorieNom);
}

