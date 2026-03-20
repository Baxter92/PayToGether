package com.ulr.paytogether.api.dto;

import com.ulr.paytogether.core.enumeration.StatutCommande;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO représentant un deal avec son statut de commande et sa moyenne
 * Utilisé pour afficher les deals d'un marchand dans l'interface admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealAvecStatutDTO {

    private UUID uuid;
    private String titre;
    private String description;
    private BigDecimal prixDeal;
    private BigDecimal prixPart;
    private BigDecimal prixPartNonReel; // Prix réel de la part (optionnel)
    private Integer nbParticipants;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private StatutDeal statut;
    private String ville;
    private String pays;
    private LocalDateTime dateCreation;
    
    // Statistiques du deal
    private Double moyenneCommentaires;      // Moyenne des notes
    private Long nombreParticipantsReel;     // Nombre de participants actuels
    private Long nombrePartsAchetees;        // Nombre de parts achetées
    
    // Statut de la commande associée
    private StatutCommande statutCommande;   // Statut de la commande liée au deal
    
    // Image principale
    private String imageUrl;                 // URL de l'image principale
}

