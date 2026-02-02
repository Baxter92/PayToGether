package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.modele.CategorieModele;

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

    void supprimerParUuid(UUID uuid);

    boolean existeParNom(String nom);
}
