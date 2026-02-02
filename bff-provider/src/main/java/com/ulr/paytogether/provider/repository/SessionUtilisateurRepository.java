package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.SessionUtilisateurJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité SessionUtilisateur
 */
@Repository
public interface SessionUtilisateurRepository extends JpaRepository<SessionUtilisateurJpa, UUID> {

    /**
     * Recherche une session par token
     * @param tokenSession le token de session
     * @return un Optional contenant la session si elle existe
     */
    Optional<SessionUtilisateurJpa> findByTokenSession(String tokenSession);

    /**
     * Recherche toutes les sessions actives d'un utilisateur
     * @param utilisateurUuid l'UUID de l'utilisateur
     * @param active true pour les sessions actives
     * @return la liste des sessions actives
     */
    List<SessionUtilisateurJpa> findByUtilisateurUuidAndActive(UUID utilisateurUuid, Boolean active);

    /**
     * Recherche toutes les sessions d'un utilisateur
     * @param utilisateurUuid l'UUID de l'utilisateur
     * @return la liste des sessions
     */
    List<SessionUtilisateurJpa> findByUtilisateurUuid(UUID utilisateurUuid);
}
