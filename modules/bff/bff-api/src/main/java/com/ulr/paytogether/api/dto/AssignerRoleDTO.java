package com.ulr.paytogether.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour l'assignation d'un rôle à un utilisateur
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignerRoleDTO {

    @NotBlank(message = "Le nom du rôle est obligatoire")
    private String nomRole;
}

