package com.ulr.paytogether.core.modele;

import com.ulr.paytogether.core.enumeration.StatutPaiement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    /**
     * Référence optionnelle au paiement (pour récupérer les infos de paiement et d'adresse)
     */
    private PaiementModele paiement;

    /**
     * Montant total payé (nombreDePart × prixPart) - Calculé ou récupéré du paiement
     */
    private BigDecimal montantTotal;

    /**
     * Statut du paiement
     */
    private StatutPaiement statutPaiement;

    /**
     * Adresse de paiement/livraison
     */
    private AdresseModele adresse;
}
