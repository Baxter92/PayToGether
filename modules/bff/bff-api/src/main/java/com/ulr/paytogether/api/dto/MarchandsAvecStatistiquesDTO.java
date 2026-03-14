package com.ulr.paytogether.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour afficher les statistiques des marchands et leur liste
 * Utilisé dans l'interface admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarchandsAvecStatistiquesDTO {

    // Statistiques globales
    private Long totalMarchands;
    private Long marchandsActifs;
    private Long marchandsInactifs;
    
    // Liste des marchands enrichis
    private List<MarchandAvecDealsDTO> marchands;
}

