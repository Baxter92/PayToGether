package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.provider.adapter.entity.CategorieJpa;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CategorieJpaMapper {

    /**
     * Convertit une entité JPA en modèle métier
     */
    public CategorieModele versModele(CategorieJpa jpaCategorie) {
        if (jpaCategorie == null) return null;
        return CategorieModele.builder()
                .uuid(jpaCategorie.getUuid())
                .nom(jpaCategorie.getNom())
                .description(jpaCategorie.getDescription())
                .icone(jpaCategorie.getIcone())
                .nbDeals(Optional.ofNullable(jpaCategorie.getDeals()).map(List::size).orElse(0))
                .dateCreation(jpaCategorie.getDateCreation())
                .dateModification(jpaCategorie.getDateModification())
                .build();
    }

    /**
     * Convertit un modèle métier en entité JPA
     */
    public CategorieJpa versEntite(CategorieModele modele) {
        if (modele == null) return null;
        return CategorieJpa.builder()
                .nom(modele.getNom())
                .description(modele.getDescription())
                .icone(modele.getIcone())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }

    /**
     * Met à jour une entité JPA existante avec les données d'un modèle métier
     */
    public void mettreAJour(CategorieJpa entite, CategorieModele modele) {
        if (entite == null || modele == null) return;

        entite.setNom(modele.getNom());
        entite.setDescription(modele.getDescription());
        entite.setIcone(modele.getIcone());
    }
}
