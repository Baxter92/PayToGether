package com.ulr.paytogether.api.dto;

import com.ulr.paytogether.core.enumeration.RoleUtilisateur;
import com.ulr.paytogether.core.enumeration.StatutUtilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO pour afficher un marchand avec ses deals et statistiques
 * Utilisé dans l'interface admin pour gérer les marchands
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarchandAvecDealsDTO {

    private UUID uuid;
    private String nom;
    private String prenom;
    private String email;
    private StatutUtilisateur statut;
    private RoleUtilisateur role;
    private String photoProfil;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    
    // Statistiques du marchand
    private Double moyenneGlobale;  // Moyenne de tous ses deals
    private Integer nombreDeals;    // Nombre total de deals
    
    // Liste des deals avec leurs infos
    private List<DealAvecStatutDTO> deals;
}

