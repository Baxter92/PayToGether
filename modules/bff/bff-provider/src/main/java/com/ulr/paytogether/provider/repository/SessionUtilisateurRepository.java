package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.SessionUtilisateurJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
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
    Optional<SessionUtilisateurJpa> findByToken(String tokenSession);


    /**
     * Recherche toutes les sessions d'un utilisateur
     * @param utilisateurJpa l'UUID de l'utilisateur
     * @return la liste des sessions
     */
    List<SessionUtilisateurJpa> findByUtilisateurJpa(UtilisateurJpa utilisateurJpa);
}
