package com.ulr.paytogether.api.dto;

import com.ulr.paytogether.core.enumeration.RoleUtilisateur;
import com.ulr.paytogether.core.enumeration.StatutUtilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour l'entité Utilisateur
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurDTO {

    private UUID uuid;

    private String nom;

    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    private StatutUtilisateur statut;

    @NotNull(message = "Le rôle est obligatoire")
    private RoleUtilisateur role;

    private String photoProfil;

    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;
}
