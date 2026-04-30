package com.ulr.paytogether.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeUtilisateurDto {
    private UUID uuid;
    private UUID commandeUuid;
    private UUID utilisateurUuid;
    private String nom;
    private String prenom;
    private String email;
    /** Statut brut de la validation (EN_ATTENTE ou VALIDEE) */
    private String statutCommandeUtilisateur;
    /** true si le statut est VALIDEE */
    private Boolean valide;
    /** Montant du paiement de cet utilisateur pour cette commande */
    private BigDecimal montant;
    /** Numéro de transaction du paiement (transactionId ou squarePaymentId) */
    private String numeroPayment;
}
