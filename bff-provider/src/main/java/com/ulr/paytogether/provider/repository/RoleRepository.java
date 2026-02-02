package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.core.domaine.entite.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Role
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Recherche un rôle par nom
     * @param nomRole le nom du rôle
     * @return un Optional contenant le rôle s'il existe
     */
    Optional<Role> findByNomRole(String nomRole);

    /**
     * Vérifie si un rôle existe avec ce nom
     * @param nomRole le nom à vérifier
     * @return true si le rôle existe
     */
    boolean existsByNomRole(String nomRole);
}
