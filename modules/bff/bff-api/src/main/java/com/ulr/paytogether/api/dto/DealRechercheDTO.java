package com.ulr.paytogether.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour les résultats de recherche de deals
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealRechercheDTO {

    private UUID uuid;
    private String titre;
    private String description;
    private BigDecimal prixDeal;
    private BigDecimal prixPart;
    private Integer nbParticipants;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateDebut;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCreation;
}

