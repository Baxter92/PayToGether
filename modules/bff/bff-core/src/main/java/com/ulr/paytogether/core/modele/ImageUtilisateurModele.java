package com.ulr.paytogether.core.modele;

import com.ulr.paytogether.core.enumeration.StatutImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier ImageModele
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUtilisateurModele {
    private UUID uuid;
    private String urlImage;
    private UUID utilisateurUuid;
    private StatutImage statut;
    private String presignUrl;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
