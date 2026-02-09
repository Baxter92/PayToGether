package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.AdresseDTO;
import com.ulr.paytogether.core.modele.AdresseModele;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre AdresseModele (Core) et AdresseDTO (API)
 */
@Component
public class AdresseMapper {

    /**
     * Convertit un modèle Core en DTO API
     */
    public AdresseDTO modeleVersDto(AdresseModele modele) {
        if (modele == null) {
            return null;
        }

        return AdresseDTO.builder()
                .uuid(modele.getUuid())
                .rue(modele.getRue())
                .ville(modele.getVille())
                .codePostal(modele.getCodePostal())
                .province(modele.getProvince())
                .pays(modele.getPays())
                .utilisateurUuid(modele.getUtilisateur() != null
                        ? modele.getUtilisateur().getUuid()
                        : null)
                .utilisateurNom(modele.getUtilisateur() != null
                        ? modele.getUtilisateur().getNom() + " " + modele.getUtilisateur().getPrenom()
                        : null)
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }

    /**
     * Convertit un DTO API en modèle Core
     */
    public AdresseModele dtoVersModele(AdresseDTO dto) {
        if (dto == null) {
            return null;
        }

        return AdresseModele.builder()
                .uuid(dto.getUuid())
                .rue(dto.getRue())
                .ville(dto.getVille())
                .codePostal(dto.getCodePostal())
                .province(dto.getProvince())
                .pays(dto.getPays())
                .dateCreation(dto.getDateCreation())
                .dateModification(dto.getDateModification())
                .build();
        // Note: L'utilisateur sera défini séparément par le service
    }
}
