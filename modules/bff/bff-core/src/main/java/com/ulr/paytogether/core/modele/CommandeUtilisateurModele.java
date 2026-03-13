package com.ulr.paytogether.core.modele;

import com.ulr.paytogether.core.enumeration.StatutCommandeUtilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier CommandeUtilisateur
 * Représente la relation entre une commande et un utilisateur avec son statut de validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeUtilisateurModele {
    private UUID uuid;
    private UUID commandeUuid;
    private UUID utilisateurUuid;
    private UtilisateurModele utilisateur;
    private StatutCommandeUtilisateur statutCommandeUtilisateur;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}

