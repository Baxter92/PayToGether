package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.ImageModele;
import com.ulr.paytogether.provider.adapter.entity.ImageJpa;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre ImageJpa (entité JPA) et ImageModele (modèle métier)
 */
@Component
public class ImageJpaMapper {

    /**
     * Convertit une entité JPA ImageJpa en modèle métier ImageModele
     *
     * @param imageJpa l'entité JPA à convertir
     * @return le modèle métier correspondant
     */
    public ImageModele versModele(ImageJpa imageJpa) {
        if (imageJpa == null) {
            return null;
        }

        return ImageModele.builder()
                .uuid(imageJpa.getUuid())
                .urlImage(imageJpa.getUrlImage())
                .statut(imageJpa.getStatut())
                .dateCreation(imageJpa.getDateCreation())
                .dateModification(imageJpa.getDateModification())
                .build();
    }

    /**
     * Convertit un modèle métier ImageModele en entité JPA ImageJpa
     *
     * @param modele le modèle métier à convertir
     * @return l'entité JPA correspondante
     */
    public ImageJpa versEntite(ImageModele modele) {
        if (modele == null) {
            return null;
        }

        return ImageJpa.builder()
                .uuid(modele.getUuid())
                .urlImage(modele.getUrlImage())
                .statut(modele.getStatut())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }

    /**
     * Met à jour une entité JPA existante avec les données d'un modèle métier
     *
     * @param entite l'entité JPA à mettre à jour
     * @param modele le modèle métier contenant les nouvelles données
     */
    public void mettreAJour(ImageJpa entite, ImageModele modele) {
        if (entite == null || modele == null) {
            return;
        }

        entite.setUrlImage(modele.getUrlImage());
        entite.setStatut(modele.getStatut());
    }
}


