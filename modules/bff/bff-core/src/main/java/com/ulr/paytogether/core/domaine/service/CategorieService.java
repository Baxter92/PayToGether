package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.modele.CategorieModele;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de service pour les opérations métier sur les catégories
 */
public interface CategorieService {

    /**
     * Créer une nouvelle catégorie
     */
    CategorieModele creer(CategorieModele categorie);

    /**
     * Lire une catégorie par son UUID
     */
    Optional<CategorieModele> lireParUuid(UUID uuid);

    /**
     * Lire une catégorie par son nom
     */
    Optional<CategorieModele> lireParNom(String nom);

    /**
     * Lire toutes les catégories
     */
    List<CategorieModele> lireTous();

    /**
     * Mettre à jour une catégorie
     */
    CategorieModele mettreAJour(UUID uuid, CategorieModele categorie);

    /**
     * Supprimer une catégorie par son UUID
     */
    void supprimerParUuid(UUID uuid);

    /**
     * Vérifier si un nom existe
     */
    boolean existeParNom(String nom);
}

