package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre JpaUtilisateur (entité JPA) et UtilisateurModele (modèle métier)
 * Ce mapper fait partie de l'adaptateur (provider)
 */
@Component
@RequiredArgsConstructor
public class UtilisateurJpaMapper {

    private final ImageUtilisateurJpaMapper imageUtilisateurJpaMapper;
    /**
     * Convertit une entité JPA en modèle métier
     */
    public UtilisateurModele versModele(UtilisateurJpa jpaUtilisateur) {
        if (jpaUtilisateur == null) {
            return null;
        }

        return UtilisateurModele.builder()
                .uuid(jpaUtilisateur.getUuid())
                .nom(jpaUtilisateur.getNom())
                .prenom(jpaUtilisateur.getPrenom())
                .email(jpaUtilisateur.getEmail())
                .motDePasse(jpaUtilisateur.getMotDePasse())
                .statut(jpaUtilisateur.getStatut())
                .role(jpaUtilisateur.getRole())
                .photoProfil(jpaUtilisateur.getPhotoProfil() != null
                        ? imageUtilisateurJpaMapper.versModele(jpaUtilisateur.getPhotoProfil())
                        : null)
                .dateCreation(jpaUtilisateur.getDateCreation())
                .dateModification(jpaUtilisateur.getDateModification())
                .build();
    }

    /**
     * Convertit un modèle métier en entité JPA
     */
    public UtilisateurJpa versEntite(UtilisateurModele modele) {
        if (modele == null) {
            return null;
        }

        return UtilisateurJpa.builder()
                .uuid(modele.getUuid())
                .nom(modele.getNom())
                .prenom(modele.getPrenom())
                .email(modele.getEmail())
                .motDePasse(modele.getMotDePasse())
                .statut(modele.getStatut())
                .role(modele.getRole())
                .photoProfil(modele.getPhotoProfil() != null
                        ? imageUtilisateurJpaMapper.versEntite(modele.getPhotoProfil())
                        : null)
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }

    /**
     * Met à jour une entité JPA avec les données du modèle métier
     */
    public void mettreAJour(UtilisateurJpa entite, UtilisateurModele modele) {
        if (entite == null || modele == null) {
            return;
        }

        entite.setNom(modele.getNom());
        entite.setPrenom(modele.getPrenom());
        entite.setEmail(modele.getEmail());
        entite.setMotDePasse(modele.getMotDePasse());
        entite.setStatut(modele.getStatut());
        entite.setRole(modele.getRole());
        entite.setPhotoProfil(modele.getPhotoProfil() != null
                ? imageUtilisateurJpaMapper.versEntite(modele.getPhotoProfil())
                : null);
    }
}
