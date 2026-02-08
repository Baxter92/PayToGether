package com.ulr.paytogether.core.modele;

import com.ulr.paytogether.core.enumeration.RoleUtilisateur;
import com.ulr.paytogether.core.enumeration.StatutUtilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier Utilisateur (indépendant de JPA)
 * Ce modèle représente un utilisateur dans le domaine métier
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurModele {

    private UUID uuid;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private StatutUtilisateur statut;
    private RoleUtilisateur role;
    private ImageUtilisateurModele photoProfil;
    private String presignUrlPhotoProfil;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
