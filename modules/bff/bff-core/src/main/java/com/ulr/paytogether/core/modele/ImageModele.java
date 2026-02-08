package com.ulr.paytogether.core.modele;

import com.ulr.paytogether.core.enumeration.StatutImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier Image (indépendant de JPA)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageModele {
    private UUID uuid;
    private String urlImage;
    private StatutImage statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
