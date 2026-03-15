package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.CommandeUtilisateurModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.provider.adapter.entity.CommandeUtilisateurJpa;
import org.springframework.stereotype.Component;

/**
 * Mapper pour CommandeUtilisateur (JPA ↔ Modèle)
 */
@Component
public class CommandeUtilisateurJpaMapper {
    
    /**
     * Convertit une entité JPA vers un modèle métier
     */
    public CommandeUtilisateurModele versModele(CommandeUtilisateurJpa jpa) {
        if (jpa == null) {
            return null;
        }
        return CommandeUtilisateurModele.builder()
            .uuid(jpa.getUuid())
            .commandeUuid(jpa.getCommandeJpa() != null ? jpa.getCommandeJpa().getUuid() : null)
            .utilisateurUuid(jpa.getUtilisateurJpa() != null ? jpa.getUtilisateurJpa().getUuid() : null)
            .statutCommandeUtilisateur(jpa.getStatutCommandeUtilisateur())
            .dateCreation(jpa.getDateCreation())
            .dateModification(jpa.getDateModification())
            .build();
    }
}

