package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.CategorieService;
import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.core.provider.CategorieProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation du service pour les catégories
 * Contient la logique métier
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategorieServiceImpl implements CategorieService {

    private final CategorieProvider categorieProvider;

    @Override
    public CategorieModele creer(CategorieModele categorie) {
        log.info("Création d'une catégorie: {}", categorie.getNom());

        // Vérifier si le nom existe déjà
        if (categorieProvider.existeParNom(categorie.getNom())) {
            throw new IllegalArgumentException("Une catégorie avec ce nom existe déjà");
        }

        return categorieProvider.sauvegarder(categorie);
    }

    @Override
    public Optional<CategorieModele> lireParUuid(UUID uuid) {
        log.debug("Lecture de la catégorie: {}", uuid);
        return categorieProvider.trouverParUuid(uuid);
    }

    @Override
    public Optional<CategorieModele> lireParNom(String nom) {
        log.debug("Lecture de la catégorie par nom: {}", nom);
        return categorieProvider.trouverParNom(nom);
    }

    @Override
    public List<CategorieModele> lireTous() {
        log.debug("Lecture de toutes les catégories");
        return categorieProvider.trouverTous();
    }

    @Override
    public CategorieModele mettreAJour(UUID uuid, CategorieModele categorie) {
        log.info("Mise à jour de la catégorie: {}", uuid);

        // Vérifier si une autre catégorie a déjà ce nom
        Optional<CategorieModele> existante = categorieProvider.trouverParNom(categorie.getNom());
        if (existante.isPresent() && !existante.get().getUuid().equals(uuid)) {
            throw new IllegalArgumentException("Une autre catégorie avec ce nom existe déjà");
        }

        return categorieProvider.mettreAJour(uuid, categorie);
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        log.info("Suppression de la catégorie: {}", uuid);
        categorieProvider.supprimerParUuid(uuid);
    }

    @Override
    public boolean existeParNom(String nom) {
        log.debug("Vérification de l'existence de la catégorie: {}", nom);
        return categorieProvider.existeParNom(nom);
    }
}

