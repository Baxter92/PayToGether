package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.core.domaine.entite.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Catégorie
 */
@Repository
public interface CategorieRepository extends JpaRepository<Categorie, UUID> {

    /**
     * Recherche une catégorie par nom
     * @param nom le nom de la catégorie
     * @return un Optional contenant la catégorie si elle existe
     */
    Optional<Categorie> findByNom(String nom);

    /**
     * Vérifie si une catégorie existe avec ce nom
     * @param nom le nom à vérifier
     * @return true si la catégorie existe
     */
    boolean existsByNom(String nom);
}
