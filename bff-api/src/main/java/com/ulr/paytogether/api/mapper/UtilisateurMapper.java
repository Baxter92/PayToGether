package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.CreerUtilisateurDTO;
import com.ulr.paytogether.api.dto.UtilisateurDTO;
import com.ulr.paytogether.provider.adapter.entity.enumeration.RoleUtilisateur;
import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutUtilisateur;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre UtilisateurModele (Core) et UtilisateurDTO (API)
 * Ce mapper fait partie de la couche API
 */
@Component
public class UtilisateurMapper {

    /**
     * Convertit un modèle Core en DTO API
     */
    public UtilisateurDTO modeleVersDto(UtilisateurModele modele) {
        if (modele == null) {
            return null;
        }

        return UtilisateurDTO.builder()
                .uuid(modele.getUuid())
                .nom(modele.getNom())
                .prenom(modele.getPrenom())
                .email(modele.getEmail())
                .statut(modele.getStatut())
                .role(modele.getRole())
                .photoProfil(modele.getPhotoProfil())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }

    /**
     * Convertit un DTO API en modèle Core
     */
    public UtilisateurModele dtoVersModele(UtilisateurDTO dto) {
        if (dto == null) {
            return null;
        }

        return UtilisateurModele.builder()
                .uuid(dto.getUuid())
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .email(dto.getEmail())
                .statut(dto.getStatut())
                .role(dto.getRole())
                .photoProfil(dto.getPhotoProfil())
                .dateCreation(dto.getDateCreation())
                .dateModification(dto.getDateModification())
                .build();
    }

    /**
     * Convertit un CreerUtilisateurDTO en modèle Core
     */
    public UtilisateurModele dtoVersModele(CreerUtilisateurDTO dto) {
        if (dto == null) {
            return null;
        }

        return UtilisateurModele.builder()
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .email(dto.getEmail())
                .motDePasse(dto.getMotDePasse())
                .photoProfil(dto.getPhotoProfil())
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.UTILISATEUR)
                .build();
    }
}
