package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.CreerUtilisateurDTO;
import com.ulr.paytogether.api.dto.UtilisateurDTO;
import com.ulr.paytogether.core.enumeration.RoleUtilisateur;
import com.ulr.paytogether.core.enumeration.StatutUtilisateur;
import com.ulr.paytogether.core.modele.ImageUtilisateurModele;
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
                    .photoProfil(modele.getPhotoProfil() != null ?
                            modele.getPhotoProfil().getUrlImage()
                            : null)
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
                .photoProfil(dto.getPhotoProfil() != null ?
                        ImageUtilisateurModele.builder()
                                .urlImage(dto.getPhotoProfil())
                                .build()
                        : null)
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
                .photoProfil(dto.getPhotoProfil() != null ?
                        ImageUtilisateurModele.builder()
                                .urlImage(dto.getPhotoProfil())
                                .build()
                        : null)
                .statut(StatutUtilisateur.ACTIF)
                .role(dto.getRole())
                .build();
    }
}
