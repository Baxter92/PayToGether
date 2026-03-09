package com.ulr.paytogether.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour les statistiques globales des paiements
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementStatsDTO {

    private Long totalTransactions;
    private Long transactionsReussies;
    private Long transactionsEchouees;
    private BigDecimal montantTotal;
}

