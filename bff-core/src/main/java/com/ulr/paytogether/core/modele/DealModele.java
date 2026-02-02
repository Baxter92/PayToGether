package com.ulr.paytogether.core.modele;

import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutDeal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Modèle métier Deal (indépendant de JPA)
 * Ce modèle représente un deal dans le domaine métier
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealModele {

    private UUID uuid;
    private String titre;
    private String description;
    private BigDecimal prixDeal;
    private BigDecimal prixPart;
    private Integer nbParticipants;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private StatutDeal statut;
    private UtilisateurModele createur;
    private CategorieModele categorie;
    private List<String> listeImages;
    private List<String> listePointsForts;
    private LocalDateTime dateExpiration;
    private String ville;
    private String pays;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
