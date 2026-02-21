package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.CategorieDTO;
import com.ulr.paytogether.core.modele.CategorieModele;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre CategorieModele (Core) et CategorieDTO (API)
 */
@Component
public class CategorieMapper {

    /**
     * Convertit un modèle Core en DTO API
     */
    public CategorieDTO modeleVersDto(CategorieModele modele) {
        if (modele == null) {
            return null;
        }

        return CategorieDTO.builder()
                .uuid(modele.getUuid())
                .nom(modele.getNom())
                .description(modele.getDescription())
                .icone(modele.getIcone())
                .nbDeals(modele.getNbDeals())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }

    /**
     * Convertit un DTO API en modèle Core
     */
    public CategorieModele dtoVersModele(CategorieDTO dto) {
        if (dto == null) {
            return null;
        }

        return CategorieModele.builder()
                .uuid(dto.getUuid())
                .nom(dto.getNom())
                .description(dto.getDescription())
                .icone(dto.getIcone())
                .dateCreation(dto.getDateCreation())
                .dateModification(dto.getDateModification())
                .build();
    }
}
