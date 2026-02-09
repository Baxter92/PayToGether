package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.ImageDealModele;
import com.ulr.paytogether.provider.adapter.entity.ImageDealJpa;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre ImageDealJpa (entité JPA) et ImageDealModele (modèle métier)
 */
@Component
public class ImageDealJpaMapper {

    /**
     * Convertit une entité JPA ImageDealJpa en modèle métier ImageDealModele
     *
     * @param imageDealJpa l'entité JPA à convertir
     * @return le modèle métier correspondant
     */
    public ImageDealModele versModele(ImageDealJpa imageDealJpa) {
        if (imageDealJpa == null) {
            return null;
        }

        return ImageDealModele.builder()
                .uuid(imageDealJpa.getUuid())
                .urlImage(imageDealJpa.getUrlImage())
                .dealUuid(imageDealJpa.getDealJpa() != null ? imageDealJpa.getDealJpa().getUuid() : null)
                .isPrincipal(imageDealJpa.getIsPrincipal())
                .statut(imageDealJpa.getStatut())
                .dateCreation(imageDealJpa.getDateCreation())
                .dateModification(imageDealJpa.getDateModification())
                .build();
    }

    /**
     * Convertit un modèle métier ImageDealModele en entité JPA ImageDealJpa
     *
     * @param modele le modèle métier à convertir
     * @return l'entité JPA correspondante
     */
    public ImageDealJpa versEntite(ImageDealModele modele) {
        if (modele == null) {
            return null;
        }

        return ImageDealJpa.builder()
                .uuid(modele.getUuid())
                .urlImage(modele.getUrlImage())
                .isPrincipal(modele.getIsPrincipal())
                .statut(modele.getStatut())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
        // Note : la relation dealJpa doit être définie séparément
    }

    /**
     * Met à jour une entité JPA existante avec les données d'un modèle métier
     *
     * @param entite l'entité JPA à mettre à jour
     * @param modele le modèle métier contenant les nouvelles données
     */
    public void mettreAJour(ImageDealJpa entite, ImageDealModele modele) {
        if (entite == null || modele == null) {
            return;
        }

        entite.setUrlImage(modele.getUrlImage());
        entite.setIsPrincipal(modele.getIsPrincipal());
        entite.setStatut(modele.getStatut());
    }
}
