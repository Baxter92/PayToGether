package com.ulr.paytogether.api.dto;

import com.ulr.paytogether.provider.adapter.entity.enumeration.RoleUtilisateur;
import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutUtilisateur;
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

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotNull(message = "Le statut est obligatoire")
    private StatutUtilisateur statut;

    @NotNull(message = "Le rôle est obligatoire")
    private RoleUtilisateur role;

    private String photoProfil;

    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;
}
