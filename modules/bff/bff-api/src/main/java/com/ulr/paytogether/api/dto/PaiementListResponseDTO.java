package com.ulr.paytogether.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la réponse complète avec liste des paiements et statistiques
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementListResponseDTO {

    private List<PaiementListDTO> paiements;
    private PaiementStatsDTO statistiques;
}

