package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Utilisateur
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<UtilisateurJpa, UUID> {

    /**
     * Recherche un utilisateur par email
     * @param email l'email de l'utilisateur
     * @return un Optional contenant l'utilisateur s'il existe
     */
    Optional<UtilisateurJpa> findByEmail(String email);

    /**
     * Vérifie si un utilisateur existe avec cet email
     * @param email l'email à vérifier
     * @return true si l'utilisateur existe
     */
    boolean existsByEmail(String email);

    /**
     * Vérifie si un utilisateur existe avec cet ID Keycloak
     * @param keycloakId l'ID Keycloak à vérifier
     * @return true si l'utilisateur existe
     */
    boolean existsByKeycloakId(String keycloakId);
}
