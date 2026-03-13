package com.ulr.paytogether.core.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Événement déclenché lorsque le vendeur upload sa facture
 * Déclenche la génération et l'envoi des factures aux clients
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerInvoiceUploadedEvent {
    private UUID commandeUuid;
    private String numeroCommande;
    private String factureMarchandUrl;
}

