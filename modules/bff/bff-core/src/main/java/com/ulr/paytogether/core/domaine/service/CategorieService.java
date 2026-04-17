package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.core.modele.PageModele;

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
     * Lire toutes les catégories avec pagination
     * @param page Numéro de la page (commence à 0)
     * @param size Taille de la page
     * @return Page de catégories
     */
    PageModele<CategorieModele> lireTous(int page, int size);

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

