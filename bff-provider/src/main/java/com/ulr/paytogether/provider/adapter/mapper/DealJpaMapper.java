package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre JpaDeal (entité JPA) et DealModele (modèle métier)
 */
@Component
public class DealJpaMapper {

    public DealModele versModele(DealJpa jpaDeal) {
        if (jpaDeal == null) {
            return null;
        }

        return DealModele.builder()
                .uuid(jpaDeal.getUuid())
                .titre(jpaDeal.getTitre())
                .description(jpaDeal.getDescription())
                .prixDeal(jpaDeal.getPrixDeal())
                .prixPart(jpaDeal.getPrixPart())
                .nbParticipants(jpaDeal.getNbParticipants())
                .dateDebut(jpaDeal.getDateDebut())
                .dateFin(jpaDeal.getDateFin())
                .statut(jpaDeal.getStatut())
                .createurUuid(jpaDeal.getCreateurUuid())
                .categorieUuid(jpaDeal.getCategorieUuid())
                .listeImages(jpaDeal.getListeImages())
                .listePointsForts(jpaDeal.getListePointsForts())
                .dateExpiration(jpaDeal.getDateExpiration())
                .ville(jpaDeal.getVille())
                .pays(jpaDeal.getPays())
                .dateCreation(jpaDeal.getDateCreation())
                .dateModification(jpaDeal.getDateModification())
                .build();
    }

    public DealJpa versEntite(DealModele modele) {
        if (modele == null) {
            return null;
        }

        return DealJpa.builder()
                .uuid(modele.getUuid())
                .titre(modele.getTitre())
                .description(modele.getDescription())
                .prixDeal(modele.getPrixDeal())
                .prixPart(modele.getPrixPart())
                .nbParticipants(modele.getNbParticipants())
                .dateDebut(modele.getDateDebut())
                .dateFin(modele.getDateFin())
                .statut(modele.getStatut())
                .createurUuid(modele.getCreateurUuid())
                .categorieUuid(modele.getCategorieUuid())
                .listeImages(modele.getListeImages())
                .listePointsForts(modele.getListePointsForts())
                .dateExpiration(modele.getDateExpiration())
                .ville(modele.getVille())
                .pays(modele.getPays())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }

    public void mettreAJour(DealJpa entite, DealModele modele) {
        if (entite == null || modele == null) {
            return;
        }

        entite.setTitre(modele.getTitre());
        entite.setDescription(modele.getDescription());
        entite.setPrixDeal(modele.getPrixDeal());
        entite.setPrixPart(modele.getPrixPart());
        entite.setNbParticipants(modele.getNbParticipants());
        entite.setDateDebut(modele.getDateDebut());
        entite.setDateFin(modele.getDateFin());
        entite.setStatut(modele.getStatut());
        entite.setCreateurUuid(modele.getCreateurUuid());
        entite.setCategorieUuid(modele.getCategorieUuid());
        entite.setListeImages(modele.getListeImages());
        entite.setListePointsForts(modele.getListePointsForts());
        entite.setDateExpiration(modele.getDateExpiration());
        entite.setVille(modele.getVille());
        entite.setPays(modele.getPays());
    }
}
