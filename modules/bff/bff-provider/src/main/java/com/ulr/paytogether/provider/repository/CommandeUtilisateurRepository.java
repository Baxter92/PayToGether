package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.core.enumeration.StatutCommandeUtilisateur;
import com.ulr.paytogether.provider.adapter.entity.CommandeUtilisateurJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository JPA pour CommandeUtilisateur
 */
@Repository
public interface CommandeUtilisateurRepository extends JpaRepository<CommandeUtilisateurJpa, UUID> {
    
    /**
     * Trouve tous les utilisateurs d'une commande
     */
    List<CommandeUtilisateurJpa> findByCommandeJpaUuid(UUID commandeUuid);

    /**
     *
     * @param utilisateurJpaUuid
     * @return
     */
    List<CommandeUtilisateurJpa> findByUtilisateurJpaUuid(UUID utilisateurJpaUuid);
    
    /**
     * Trouve un utilisateur spécifique d'une commande
     */
    Optional<CommandeUtilisateurJpa> findByCommandeJpaUuidAndUtilisateurJpaUuid(UUID commandeUuid, UUID utilisateurUuid);
    
    /**
     * Compte le nombre d'utilisateurs validés pour une commande
     */
    long countByCommandeJpaUuidAndStatutCommandeUtilisateur(UUID commandeUuid, StatutCommandeUtilisateur statut);
}

