package com.ulr.paytogether.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les statistiques globales des commandes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeStatsDTO {

    private Long totalCommandes;
    private Long commandesConfirmees;
    private Long commandesEnCours;
    private Long commandesAnnulees;
    private Long commandesRemboursees;
}

