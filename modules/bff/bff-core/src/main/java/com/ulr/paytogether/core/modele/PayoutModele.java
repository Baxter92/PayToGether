package com.ulr.paytogether.core.modele;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier Payout (indépendant de JPA)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutModele {

    private UUID uuid;
    private BigDecimal montant;
    private String statut;
    private UUID utilisateurUuid;
    private String methodePaiement;
    private LocalDateTime datePayout;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
