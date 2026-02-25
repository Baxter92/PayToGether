package com.ulr.paytogether.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour le transfert de données des commentaires
 * Validation Jakarta pour première ligne de défense
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentaireDTO {

    private UUID uuid;

    @NotBlank(message = "Le contenu du commentaire est obligatoire")
    @Size(min = 1, max = 2000, message = "Le contenu doit contenir entre 1 et 2000 caractères")
    private String contenu;

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note doit être au minimum 1")
    @Max(value = 5, message = "La note doit être au maximum 5")
    private Integer note;

    @NotNull(message = "L'UUID de l'utilisateur est obligatoire")
    private UUID utilisateurUuid;

    @NotNull(message = "L'UUID du deal est obligatoire")
    private UUID dealUuid;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCreation;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateModification;
}

