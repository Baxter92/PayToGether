package com.ulr.paytogether.core.modele;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier Commentaire (indépendant de JPA)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentaireModele {

    private UUID uuid;
    private String contenu;
    private Integer note;
    private UtilisateurModele utilisateur;
    private DealModele deal;

    /**
     * UUID du commentaire parent (null si c'est un commentaire racine)
     */
    private UUID commentaireParentUuid;

    /**
     * Flag indiquant si la réponse est marquée comme pertinente
     * Utilisé uniquement pour les réponses (commentaireParentUuid != null)
     */
    private Boolean estPertinent;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
