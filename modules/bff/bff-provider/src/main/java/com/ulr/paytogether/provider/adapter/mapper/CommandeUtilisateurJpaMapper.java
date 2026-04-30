package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.CommandeUtilisateurModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.provider.adapter.entity.CommandeUtilisateurJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import org.springframework.stereotype.Component;

/**
 * Mapper pour CommandeUtilisateur (JPA ↔ Modèle)
 */
@Component
public class CommandeUtilisateurJpaMapper {

    /**
     * Convertit une entité JPA vers un modèle métier
     * Note : montant et numeroPayment sont populés séparément par le ProviderAdapter
     */
    public CommandeUtilisateurModele versModele(CommandeUtilisateurJpa jpa) {
        if (jpa == null) {
            return null;
        }

        // Mapper l'utilisateur depuis JPA (accessible dans la session Hibernate)
        UtilisateurModele utilisateurModele = null;
        if (jpa.getUtilisateurJpa() != null) {
            UtilisateurJpa u = jpa.getUtilisateurJpa();
            utilisateurModele = UtilisateurModele.builder()
                .uuid(u.getUuid())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .build();
        }

        return CommandeUtilisateurModele.builder()
            .uuid(jpa.getUuid())
            .commandeUuid(jpa.getCommandeJpa() != null ? jpa.getCommandeJpa().getUuid() : null)
            .utilisateurUuid(jpa.getUtilisateurJpa() != null ? jpa.getUtilisateurJpa().getUuid() : null)
            .utilisateur(utilisateurModele)
            .statutCommandeUtilisateur(jpa.getStatutCommandeUtilisateur())
            .dateCreation(jpa.getDateCreation())
            .dateModification(jpa.getDateModification())
            .build();
    }
}
