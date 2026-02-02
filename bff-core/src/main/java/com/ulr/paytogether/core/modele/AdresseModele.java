package com.ulr.paytogether.core.modele;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier Adresse (indépendant de JPA)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdresseModele {

    private UUID uuid;
    private String rue;
    private String ville;
    private String codePostal;
    private String province;
    private String pays;
    private UtilisateurModele utilisateur;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
