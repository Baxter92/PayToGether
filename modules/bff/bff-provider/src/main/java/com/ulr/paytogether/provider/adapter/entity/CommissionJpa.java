package com.ulr.paytogether.provider.adapter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entité représentant une commission
 */
@Entity
@Table(name = "commission")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionJpa extends BaseEntite {

    @NotNull(message = "Le pourcentage de commission est obligatoire")
    @Positive(message = "Le pourcentage de commission doit être positif")
    @Column(name = "pourcentage_commission", nullable = false, precision = 5, scale = 2)
    private BigDecimal pourcentageCommission;

    @Column(name = "montant_minimum", precision = 10, scale = 2)
    private BigDecimal montantMinimum;

    @Column(name = "montant", precision = 10, scale = 2)
    private BigDecimal montant;
}
