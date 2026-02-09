package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.ImageUtilisateurModele;
import com.ulr.paytogether.provider.adapter.entity.ImageUtilisateurJpa;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre ImageUtilisateurJpa (entité JPA) et ImageUtilisateurModele (modèle métier)
 */
@Component
public class ImageUtilisateurJpaMapper {

    /**
     * Convertit une entité JPA ImageUtilisateurJpa en modèle métier ImageUtilisateurModele
     *
     * @param imageUtilisateurJpa l'entité JPA à convertir
     * @return le modèle métier correspondant
     */
    public ImageUtilisateurModele versModele(ImageUtilisateurJpa imageUtilisateurJpa) {
        if (imageUtilisateurJpa == null) {
            return null;
        }

        return ImageUtilisateurModele.builder()
                .uuid(imageUtilisateurJpa.getUuid())
                .urlImage(imageUtilisateurJpa.getUrlImage())
                .utilisateurUuid(imageUtilisateurJpa.getUtilisateurJpa() != null
                        ? imageUtilisateurJpa.getUtilisateurJpa().getUuid()
                        : null)
                .statut(imageUtilisateurJpa.getStatut())
                .dateCreation(imageUtilisateurJpa.getDateCreation())
                .dateModification(imageUtilisateurJpa.getDateModification())
                .build();
    }

    /**
     * Convertit un modèle métier ImageUtilisateurModele en entité JPA ImageUtilisateurJpa
     *
     * @param modele le modèle métier à convertir
     * @return l'entité JPA correspondante
     */
    public ImageUtilisateurJpa versEntite(ImageUtilisateurModele modele) {
        if (modele == null) {
            return null;
        }

        return ImageUtilisateurJpa.builder()
                .uuid(modele.getUuid())
                .urlImage(modele.getUrlImage())
                .statut(modele.getStatut())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
        // Note : la relation utilisateurJpa doit être définie séparément
    }

    /**
     * Met à jour une entité JPA existante avec les données d'un modèle métier
     *
     * @param entite l'entité JPA à mettre à jour
     * @param modele le modèle métier contenant les nouvelles données
     */
    public void mettreAJour(ImageUtilisateurJpa entite, ImageUtilisateurModele modele) {
        if (entite == null || modele == null) {
            return;
        }

        entite.setUrlImage(modele.getUrlImage());
        entite.setStatut(modele.getStatut());
    }
}
