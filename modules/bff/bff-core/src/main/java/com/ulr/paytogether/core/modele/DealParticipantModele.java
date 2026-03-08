package com.ulr.paytogether.core.modele;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier pour la participation d'un utilisateur à un deal
 * Représente la relation ManyToMany entre Deal et Utilisateur
 * avec le nombre de parts achetées
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealParticipantModele {

    /**
     * UUID du deal
     */
    private UUID dealUuid;

    /**
     * UUID de l'utilisateur participant
     */
    private UUID utilisateurUuid;

    /**
     * Nombre de parts achetées par le participant
     */
    @Builder.Default
    private Integer nombreDePart = 1;

    /**
     * Date de participation au deal
     */
    private LocalDateTime dateParticipation;

    /**
     * Date de dernière modification
     */
    private LocalDateTime dateModification;

    /**
     * Référence optionnelle au deal complet (pour éviter les requêtes multiples)
     */
    private DealModele deal;

    /**
     * Référence optionnelle à l'utilisateur complet (pour éviter les requêtes multiples)
     */
    private UtilisateurModele utilisateur;
}

