package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.modele.CommentaireModele;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface du service Commentaire
 */
public interface CommentaireService {

    /**
     * Créer un commentaire
     */
    CommentaireModele creer(CommentaireModele commentaireModele);

    /**
     * Lire un commentaire par son UUID
     */
    Optional<CommentaireModele> lireParUuid(UUID uuid);

    /**
     * Lire tous les commentaires d'un deal
     */
    List<CommentaireModele> lireTous(UUID uuid);


    /**
     * Mettre à jour un commentaire
     */
    CommentaireModele mettreAJour(UUID uuid, CommentaireModele paiement);

    /**
     * Supprimer un paiement par son UUID
     */
    void supprimerParUuid(UUID uuid);
}