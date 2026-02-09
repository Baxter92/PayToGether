package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.modele.PubliciteModele;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de service pour les opérations métier sur les publicités
 */
public interface PubliciteService {

    /**
     * Créer une nouvelle publicité
     */
    PubliciteModele creer(PubliciteModele publicite);

    /**
     * Lire une publicité par son UUID
     */
    Optional<PubliciteModele> lireParUuid(UUID uuid);

    /**
     * Lire toutes les publicités
     */
    List<PubliciteModele> lireTous();

    /**
     * Lire les publicités actives
     */
    List<PubliciteModele> lireActives();

    /**
     * Mettre à jour une publicité
     */
    PubliciteModele mettreAJour(UUID uuid, PubliciteModele publicite);

    /**
     * Supprimer une publicité par son UUID
     */
    void supprimerParUuid(UUID uuid);
}
