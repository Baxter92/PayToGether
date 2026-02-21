package com.ulr.paytogether.core.modele;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Modèle métier pour la réponse getMe
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeResponseModele {

    private String id;
    private String username;
    private String email;
    private String prenom;
    private String nom;
    private boolean actif;
    private boolean emailVerifie;
    private Long dateCreationTimestamp;
    private List<String> roles;
}

