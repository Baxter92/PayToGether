package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.core.enumeration.RoleUtilisateur;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
        * Recherche un utilisateur par son ID Keycloak
        * @param keycloakId l'ID Keycloak de l'utilisateur
        * @return un Optional contenant l'utilisateur s'il existe
        */
    Optional<UtilisateurJpa> findByKeycloakId(String keycloakId);

    /**
     * Recherche tous les utilisateurs par rôle
     * @param role le rôle
     * @return liste des utilisateurs
     */
    List<UtilisateurJpa> findByRole(RoleUtilisateur role);

    /**
     * Recherche tous les utilisateurs par rôle avec pagination
     * @param role le rôle
     * @param pageable paramètres de pagination
     * @return page d'utilisateurs
     */
    Page<UtilisateurJpa> findByRole(RoleUtilisateur role, Pageable pageable);
}
