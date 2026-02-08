package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.PubliciteModele;
import com.ulr.paytogether.provider.adapter.entity.ImageJpa;
import com.ulr.paytogether.provider.adapter.entity.PubliciteJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper pour convertir entre PubliciteJpa (entité JPA) et PubliciteModele (modèle métier)
 */
@Component
@RequiredArgsConstructor
public class PubliciteJpaMapper {

    private final ImageJpaMapper imageJpaMapper;
    /**
     * Convertit une entité JPA PubliciteJpa en modèle métier PubliciteModele
     *
     * @param publiciteJpa l'entité JPA à convertir
     * @return le modèle métier correspondant
     */
    public PubliciteModele versModele(PubliciteJpa publiciteJpa) {
        if (publiciteJpa == null) {
            return null;
        }

        return PubliciteModele.builder()
                .uuid(publiciteJpa.getUuid())
                .titre(publiciteJpa.getTitre())
                .description(publiciteJpa.getDescription())
                .listeImages(publiciteJpa.getListeImages() != null
                        ? publiciteJpa.getListeImages().stream()
                        .map(imageJpaMapper::versModele).toList() : null)
                .dateDebut(publiciteJpa.getDateDebut())
                .dateFin(publiciteJpa.getDateFin())
                .active(publiciteJpa.getActive())
                .dateCreation(publiciteJpa.getDateCreation())
                .dateModification(publiciteJpa.getDateModification())
                .build();
    }

    /**
     * Convertit un modèle métier PubliciteModele en entité JPA PubliciteJpa
     *
     * @param modele le modèle métier à convertir
     * @return l'entité JPA correspondante
     */
    public PubliciteJpa versEntite(PubliciteModele modele) {
        if (modele == null) {
            return null;
        }

        PubliciteJpa publiciteJpa = PubliciteJpa.builder()
                .uuid(modele.getUuid())
                .titre(modele.getTitre())
                .description(modele.getDescription())
                .dateDebut(modele.getDateDebut())
                .dateFin(modele.getDateFin())
                .active(modele.getActive())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();

        // Conversion des images en entités ImageJpa (si nécessaire)
        if (modele.getListeImages() != null && !modele.getListeImages().isEmpty()) {
            List<ImageJpa> imagesJpa = modele.getListeImages().stream()
                    .map(imageJpaMapper::versEntite)
                    .toList();
            publiciteJpa.setListeImages(imagesJpa);
        }

        return publiciteJpa;
    }

    /**
     * Met à jour une entité JPA existante avec les données d'un modèle métier
     *
     * @param entite l'entité JPA à mettre à jour
     * @param modele le modèle métier contenant les nouvelles données
     */
    public void mettreAJour(PubliciteJpa entite, PubliciteModele modele) {
        if (entite == null || modele == null) {
            return;
        }

        entite.setTitre(modele.getTitre());
        entite.setDescription(modele.getDescription());
        entite.setDateDebut(modele.getDateDebut());
        entite.setDateFin(modele.getDateFin());
        entite.setActive(modele.getActive());

        // Mise à jour de la liste des images
        if (modele.getListeImages() != null) {
            entite.getListeImages().clear();
            List<ImageJpa> nouvellesImages = modele.getListeImages().stream()
                    .map(imageJpaMapper::versEntite)
                    .toList();
            entite.getListeImages().addAll(nouvellesImages);
        }
    }
}
