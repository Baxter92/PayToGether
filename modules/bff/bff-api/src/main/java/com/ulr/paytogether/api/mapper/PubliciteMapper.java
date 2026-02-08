package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.PubliciteDTO;
import com.ulr.paytogether.core.modele.ImageModele;
import com.ulr.paytogether.core.modele.PubliciteModele;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper pour convertir entre PubliciteDTO et PubliciteModele
 */
@Component
public class PubliciteMapper {

    /**
     * Convertit un DTO en modèle métier
     */
    public PubliciteModele dtoVersModele(PubliciteDTO dto) {
        if (dto == null) {
            return null;
        }

        return PubliciteModele.builder()
                .uuid(dto.getUuid())
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .lienExterne(dto.getLienExterne())
                .listeImages(dto.getListeImages() != null
                        ? dto.getListeImages().stream()
                        .map(url -> ImageModele.builder().urlImage(url).build())
                        .toList()
                        : null)
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .active(dto.getActive())
                .dateCreation(dto.getDateCreation())
                .dateModification(dto.getDateModification())
                .build();
    }

    /**
     * Convertit un modèle métier en DTO
     */
    public PubliciteDTO modeleVersDto(PubliciteModele modele) {
        if (modele == null) {
            return null;
        }

        return PubliciteDTO.builder()
                .uuid(modele.getUuid())
                .titre(modele.getTitre())
                .description(modele.getDescription())
                .lienExterne(modele.getLienExterne())
                .listeImages(modele.getListeImages() != null
                        ? modele.getListeImages().stream()
                        .map(ImageModele::getUrlImage)
                        .toList()
                        : null)
                .dateDebut(modele.getDateDebut())
                .dateFin(modele.getDateFin())
                .active(modele.getActive())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }
}
