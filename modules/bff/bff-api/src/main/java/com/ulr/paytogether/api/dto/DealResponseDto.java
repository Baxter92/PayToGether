package com.ulr.paytogether.api.dto;

import com.ulr.paytogether.core.enumeration.StatutDeal;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DealResponseDto{
        private UUID uuid;

private String titre;

private String description;

private BigDecimal prixDeal;

private BigDecimal prixPart;

private Integer nbParticipants;

private LocalDateTime dateDebut;

private LocalDateTime dateFin;

private StatutDeal statut;

private UUID createurUuid;

private String createurNom;

private UUID categorieUuid;

private String categorieNom;

private List<ImageDealDto> listeImages;

private List<String> listePointsForts;

private LocalDateTime dateExpiration;
private String ville;
private String pays;

private LocalDateTime dateCreation;
private LocalDateTime dateModification;
}