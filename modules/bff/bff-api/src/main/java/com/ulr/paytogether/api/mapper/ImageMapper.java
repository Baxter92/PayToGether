package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.ImageDto;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.ImageModele;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre ImageDto et ImageModele
 */
@Component
public class ImageMapper {

    /**
     * Convertit un modèle Image en DTO
     *
     * @param modele le modèle à convertir
     * @return le DTO correspondant
     */
    public ImageDto modeleVersDto(ImageModele modele) {
        if (modele == null) {
            return null;
        }

        return new ImageDto(
                modele.getUuid(),
                modele.getUrlImage(),
                modele.getPresignUrl(),
                modele.getStatut()
        );
    }

    /**
     * Convertit un DTO Image en modèle
     *
     * @param dto le DTO à convertir
     * @return le modèle correspondant
     */
    public ImageModele dtoVersModele(ImageDto dto) {
        if (dto == null) {
            return null;
        }

        return ImageModele.builder()
                .uuid(dto.imageUuid())
                .urlImage(dto.urlImage())
                .presignUrl(dto.presignUrl())
                .statut(dto.statut() != null ? dto.statut() : StatutImage.PENDING)
                .build();
    }

    /**
     * Met à jour un modèle existant avec les données du DTO
     *
     * @param modele le modèle à mettre à jour
     * @param dto    le DTO contenant les nouvelles données
     */
    public void mettreAJour(ImageModele modele, ImageDto dto) {
        if (modele == null || dto == null) {
            return;
        }

        if (dto.urlImage() != null) {
            modele.setUrlImage(dto.urlImage());
        }
        if (dto.presignUrl() != null) {
            modele.setPresignUrl(dto.presignUrl());
        }
        if (dto.statut() != null) {
            modele.setStatut(dto.statut());
        }
    }
}

