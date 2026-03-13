package com.ulr.paytogether.core.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement déclenché lorsqu'un payout est validé par l'admin
 * Envoie un mail au vendeur pour lui demander d'uploader sa facture
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutValidatedEvent {
    private UUID commandeUuid;
    private String numeroCommande;
    private UUID vendeurUuid;
    private String emailVendeur;
    private String nomVendeur;
    private LocalDateTime dateDepotPayout;
}

