package com.ulr.paytogether.core.modele;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier pour les tokens de validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationTokenModele {
    private UUID uuid;
    private String token;
    private UUID utilisateurUuid;
    private LocalDateTime dateExpiration;
    private String typeToken;
    private Boolean utilise;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}

