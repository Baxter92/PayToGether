package com.ulr.paytogether.core.modele;

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
 * Modèle métier représentant un deal avec son statut de commande et ses statistiques
 * Utilisé pour les vues enrichies (admin, statistiques)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealAvecStatutModele {

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

