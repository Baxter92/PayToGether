package com.ulr.paytogether.core.modele;

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
 * Modèle métier représentant un marchand avec ses deals enrichis
 * Utilisé pour les statistiques et l'interface admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarchandAvecDealsModele {

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
    
    // Liste des deals avec leurs statistiques
    private List<DealAvecStatutModele> deals;
}

