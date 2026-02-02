package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.core.domaine.entite.SessionUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité SessionUtilisateur
 */
@Repository
public interface SessionUtilisateurRepository extends JpaRepository<SessionUtilisateur, UUID> {

    /**
     * Recherche une session par token
     * @param tokenSession le token de session
     * @return un Optional contenant la session si elle existe
     */
    Optional<SessionUtilisateur> findByTokenSession(String tokenSession);

    /**
     * Recherche toutes les sessions actives d'un utilisateur
     * @param utilisateurUuid l'UUID de l'utilisateur
     * @param active true pour les sessions actives
     * @return la liste des sessions actives
     */
    List<SessionUtilisateur> findByUtilisateurUuidAndActive(UUID utilisateurUuid, Boolean active);

    /**
     * Recherche toutes les sessions d'un utilisateur
     * @param utilisateurUuid l'UUID de l'utilisateur
     * @return la liste des sessions
     */
    List<SessionUtilisateur> findByUtilisateurUuid(UUID utilisateurUuid);
}
