package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.ValidationTokenModele;
import com.ulr.paytogether.provider.adapter.entity.ValidationTokenJpa;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir ValidationTokenJpa ↔ ValidationTokenModele
 */
@Component
public class ValidationTokenJpaMapper {

    /**
     * Convertir l'entité JPA en modèle métier
     */
    public ValidationTokenModele versModele(ValidationTokenJpa jpa) {
        if (jpa == null) {
            return null;
        }

        return ValidationTokenModele.builder()
                .uuid(jpa.getUuid())
                .token(jpa.getToken())
                .utilisateurUuid(jpa.getUtilisateurUuid())
                .dateExpiration(jpa.getDateExpiration())
                .typeToken(jpa.getTypeToken())
                .utilise(jpa.getUtilise())
                .dateCreation(jpa.getDateCreation())
                .dateModification(jpa.getDateModification())
                .build();
    }

    /**
     * Convertir le modèle métier en entité JPA
     */
    public ValidationTokenJpa versEntite(ValidationTokenModele modele) {
        if (modele == null) {
            return null;
        }

        return ValidationTokenJpa.builder()
                .uuid(modele.getUuid())
                .token(modele.getToken())
                .utilisateurUuid(modele.getUtilisateurUuid())
                .dateExpiration(modele.getDateExpiration())
                .typeToken(modele.getTypeToken())
                .utilise(modele.getUtilise())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }
}

