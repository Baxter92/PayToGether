package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.modele.PubliciteModele;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour les opérations sur les publicités
 * Cette interface définit le contrat que l'adaptateur (provider) doit implémenter
 * Architecture Hexagonale : c'est un port de sortie (driven port)
 */
public interface PubliciteProvider {

    /**
     * Sauvegarder une publicité
     */
    PubliciteModele sauvegarder(PubliciteModele publicite);

    /**
     * Trouver une publicité par son UUID
     */
    Optional<PubliciteModele> trouverParUuid(UUID uuid);

    /**
     * Trouver toutes les publicités
     */
    List<PubliciteModele> trouverTous();

    /**
     * Trouver les publicités actives
     */
    List<PubliciteModele> trouverActives();

    /**
     * Mettre à jour une publicité
     */
    PubliciteModele mettreAJour(UUID uuid, PubliciteModele publicite);

    /**
     * Supprimer une publicité par son UUID
     */
    void supprimerParUuid(UUID uuid);
}
