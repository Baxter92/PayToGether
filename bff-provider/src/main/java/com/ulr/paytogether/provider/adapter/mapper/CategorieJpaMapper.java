package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.provider.adapter.entity.CategorieJpa;
import org.springframework.stereotype.Component;

@Component
public class CategorieJpaMapper {
    public CategorieModele versModele(CategorieJpa jpaCategorie) {
        if (jpaCategorie == null) return null;
        return CategorieModele.builder()
                .uuid(jpaCategorie.getUuid())
                .nom(jpaCategorie.getNom())
                .description(jpaCategorie.getDescription())
                .icone(jpaCategorie.getIcone())
                .dateCreation(jpaCategorie.getDateCreation())
                .dateModification(jpaCategorie.getDateModification())
                .build();
    }
    public CategorieJpa versEntite(CategorieModele modele) {
        if (modele == null) return null;
        return CategorieJpa.builder()
                .uuid(modele.getUuid())
                .nom(modele.getNom())
                .description(modele.getDescription())
                .icone(modele.getIcone())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }
}
