package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.DealParticipantModele;
import com.ulr.paytogether.provider.adapter.entity.DealParticipantJpa;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre DealParticipantJpa et DealParticipantModele
 */
@Component
public class DealParticipantJpaMapper {

    /**
     * Convertit une entité JPA en modèle métier
     * @param jpa Entité JPA
     * @return Modèle métier
     */
    public DealParticipantModele versModele(DealParticipantJpa jpa) {
        if (jpa == null) {
            return null;
        }

        return DealParticipantModele.builder()
                .dealUuid(jpa.getId() != null ? jpa.getId().getDealUuid() : null)
                .utilisateurUuid(jpa.getId() != null ? jpa.getId().getUtilisateurUuid() : null)
                .nombreDePart(jpa.getNombreDePart())
                .dateParticipation(jpa.getDateParticipation())
                .dateModification(jpa.getDateModification())
                .build();
    }

    /**
     * Convertit un modèle métier en entité JPA
     * @param modele Modèle métier
     * @return Entité JPA
     */
    public DealParticipantJpa versEntite(DealParticipantModele modele) {
        if (modele == null) {
            return null;
        }

        DealParticipantJpa.DealParticipantId id = new DealParticipantJpa.DealParticipantId(
                modele.getDealUuid(),
                modele.getUtilisateurUuid()
        );

        return DealParticipantJpa.builder()
                .id(id)
                .nombreDePart(modele.getNombreDePart() != null ? modele.getNombreDePart() : 1)
                .dateParticipation(modele.getDateParticipation())
                .dateModification(modele.getDateModification())
                .build();
    }

    /**
     * Met à jour une entité JPA existante avec les données du modèle
     * @param jpa Entité JPA à mettre à jour
     * @param modele Modèle métier source
     */
    public void mettreAJour(DealParticipantJpa jpa, DealParticipantModele modele) {
        if (jpa == null || modele == null) {
            return;
        }

        if (modele.getNombreDePart() != null) {
            jpa.setNombreDePart(modele.getNombreDePart());
        }
    }
}

