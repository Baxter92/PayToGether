package com.ulr.paytogether.core.modele;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier Categorie (indépendant de JPA)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorieModele {

    private UUID uuid;
    private String nom;
    private String description;
    private String icone;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
