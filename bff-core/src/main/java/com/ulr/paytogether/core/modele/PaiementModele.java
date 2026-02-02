package com.ulr.paytogether.core.modele;

import com.ulr.paytogether.provider.adapter.entity.enumeration.MethodePaiement;
import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutPaiement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier Paiement (indépendant de JPA)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementModele {

    private UUID uuid;
    private BigDecimal montant;
    private StatutPaiement statut;
    private MethodePaiement methodePaiement;
    private String transactionId;
    private UtilisateurModele utilisateur;
    private CommandeModele commande;
    private LocalDateTime datePaiement;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
