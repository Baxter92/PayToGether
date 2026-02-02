package com.ulr.paytogether.core.modele;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Modèle métier Publicite (indépendant de JPA)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PubliciteModele {

    private UUID uuid;
    private String titre;
    private String description;
    private String lienExterne;
    private List<String> listeImages;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private Boolean active;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
