package com.ulr.paytogether.core.modele;

import com.ulr.paytogether.core.enumeration.StatutImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier ImageDeal (indépendant de JPA)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDealModele {

    private UUID uuid;
    private String urlImage;
    private UUID dealUuid;
    private Boolean isPrincipal;
    private StatutImage statut;
    private String presignUrl;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
