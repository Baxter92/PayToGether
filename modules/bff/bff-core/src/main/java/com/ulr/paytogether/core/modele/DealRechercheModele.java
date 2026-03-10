package com.ulr.paytogether.core.modele;

import com.ulr.paytogether.core.enumeration.StatutDeal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier pour les résultats de recherche de deals
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealRechercheModele {

    private UUID uuid;
    private String titre;
    private String description;
    private BigDecimal prixDeal;
    private BigDecimal prixPart;
    private Integer nbParticipants;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private StatutDeal statut;
    private String ville;
    private String pays;
    private UUID categorieUuid;
    private String categorieNom;
    private UUID createurUuid;
    private String createurNom;
    private String imagePrincipaleUrl;
    private int nombreDeVues;
    private LocalDateTime dateCreation;
}

