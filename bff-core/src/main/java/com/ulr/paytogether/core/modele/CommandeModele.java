package com.ulr.paytogether.core.modele;

import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutCommande;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Modèle métier Commande (indépendant de JPA)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeModele {

    private UUID uuid;
    private BigDecimal montantTotal;
    private StatutCommande statut;
    private UtilisateurModele utilisateur;
    private DealModele dealModele;
    private List<PaiementModele> paiements;
    private LocalDateTime dateCommande;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
