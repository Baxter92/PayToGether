package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.core.modele.PageModele;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour les opérations sur les catégories
 */
public interface CategorieProvider {

    CategorieModele sauvegarder(CategorieModele categorie);

    Optional<CategorieModele> trouverParUuid(UUID uuid);

    Optional<CategorieModele> trouverParNom(String nom);

    List<CategorieModele> trouverTous();

    /**
     * Trouver toutes les catégories avec pagination
     * @param page Numéro de la page (commence à 0)
     * @param size Taille de la page
     * @return Page de catégories
     */
    PageModele<CategorieModele> trouverTous(int page, int size);

    CategorieModele mettreAJour(UUID uuid, CategorieModele categorie);

    void supprimerParUuid(UUID uuid);

    boolean existeParNom(String nom);
}
