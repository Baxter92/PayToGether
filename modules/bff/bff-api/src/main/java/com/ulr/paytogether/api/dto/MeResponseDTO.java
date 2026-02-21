package com.ulr.paytogether.api.dto;

import com.ulr.paytogether.core.enumeration.RoleUtilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la réponse getMe
 * Contient les informations de l'utilisateur connecté
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeResponseDTO {

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

