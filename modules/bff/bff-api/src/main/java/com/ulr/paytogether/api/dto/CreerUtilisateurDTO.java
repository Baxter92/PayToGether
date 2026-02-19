package com.ulr.paytogether.api.dto;

import com.ulr.paytogether.core.enumeration.RoleUtilisateur;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour créer un utilisateur (sans UUID)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreerUtilisateurDTO {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotNull(message = "Le rôle est obligatoire")
    private RoleUtilisateur role;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;

    private String photoProfil;
}
