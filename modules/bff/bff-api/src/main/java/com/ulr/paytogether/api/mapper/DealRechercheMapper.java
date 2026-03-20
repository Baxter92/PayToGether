package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.DealRechercheDTO;
import com.ulr.paytogether.core.modele.DealRechercheModele;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir entre DealRechercheModele et DealRechercheDTO
 */
@Component
public class DealRechercheMapper {

    /**
     * Convertit un DealRechercheModele vers DealRechercheDTO
     * @param modele Modèle métier
     * @return DTO pour l'API
     */
    public DealRechercheDTO modeleVersDto(DealRechercheModele modele) {
        if (modele == null) {
            return null;
        }

        return DealRechercheDTO.builder()
                .uuid(modele.getUuid())
                .titre(modele.getTitre())
                .description(modele.getDescription())
                .prixDeal(modele.getPrixDeal())
                .prixPart(modele.getPrixPart())
                .prixPartNonReel(modele.getPrixPartNonReel())
                .nbParticipants(modele.getNbParticipants())
                .dateDebut(modele.getDateDebut())
                .dateFin(modele.getDateFin())
                .statut(modele.getStatut())
                .ville(modele.getVille())
                .pays(modele.getPays())
                .categorieUuid(modele.getCategorieUuid())
                .categorieNom(modele.getCategorieNom())
                .createurUuid(modele.getCreateurUuid())
                .createurNom(modele.getCreateurNom())
                .imagePrincipaleUrl(modele.getImagePrincipaleUrl())
                .nombreDeVues(modele.getNombreDeVues())
                .dateCreation(modele.getDateCreation())
                .build();
    }

    /**
     * Convertit une liste de modèles vers une liste de DTOs
     * @param modeles Liste de modèles
     * @return Liste de DTOs
     */
    public List<DealRechercheDTO> modelesVersDto(List<DealRechercheModele> modeles) {
        if (modeles == null) {
            return List.of();
        }

        return modeles.stream()
                .map(this::modeleVersDto)
                .collect(Collectors.toList());
    }
}

