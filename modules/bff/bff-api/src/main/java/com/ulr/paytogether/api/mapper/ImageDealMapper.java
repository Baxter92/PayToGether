package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.ImageDealDto;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.ImageDealModele;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre ImageDealModele et ImageDealDto
 */
@Component
public class ImageDealMapper {

    /**
     * Convertit un modèle ImageDeal en DTO
     *
     * @param modele le modèle à convertir
     * @return le DTO correspondant
     */
    public ImageDealDto modeleVersDto(ImageDealModele modele) {
        if (modele == null) {
            return null;
        }

        return new ImageDealDto(
                modele.getUrlImage(),
                modele.getIsPrincipal(),
                modele.getPresignUrl(),
                modele.getStatut()
        );
    }

    /**
     * Convertit un DTO ImageDeal en modèle
     *
     * @param dto le DTO à convertir
     * @return le modèle correspondant
     */
    public ImageDealModele dtoVersModele(ImageDealDto dto) {
        if (dto == null) {
            return null;
        }

        return ImageDealModele.builder()
                .urlImage(dto.urlImage())
                .isPrincipal(dto.isPrincipal())
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
    public void mettreAJour(ImageDealModele modele, ImageDealDto dto) {
        if (modele == null || dto == null) {
            return;
        }

        if (dto.urlImage() != null) {
            modele.setUrlImage(dto.urlImage());
        }
        if (dto.isPrincipal() != null) {
            modele.setIsPrincipal(dto.isPrincipal());
        }
        if (dto.presignUrl() != null) {
            modele.setPresignUrl(dto.presignUrl());
        }
        if (dto.statut() != null) {
            modele.setStatut(dto.statut());
        }
    }
}

