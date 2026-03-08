package com.ulr.paytogether.core.modele;

import com.ulr.paytogether.core.enumeration.MethodePaiement;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier Paiement (indépendant de JPA)
 * Support Square Payment avec champs additionnels
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

    // Champs spécifiques Square Payment
    private String squarePaymentId;      // ID du paiement Square
    private String squareOrderId;        // ID de la commande Square
    private String squareLocationId;     // ID du lieu Square
    private String squareReceiptUrl;     // URL du reçu Square
    private String squareToken;          // Token de paiement Square (sécurisé)
    private String messageErreur;        // Message d'erreur en cas d'échec

    private UtilisateurModele utilisateur;
    private CommandeModele commande;
    private DealModele deal;
    private int nombreDePart; // Nombre de parts achetées (pour les paiements liés à des deals)
    private LocalDateTime datePaiement;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    private AdresseModele adresse;
}
