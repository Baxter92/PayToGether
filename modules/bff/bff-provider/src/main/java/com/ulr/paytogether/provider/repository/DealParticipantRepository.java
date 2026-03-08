package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.DealParticipantJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour gérer les participations aux deals
 */
@Repository
public interface DealParticipantRepository extends JpaRepository<DealParticipantJpa, DealParticipantJpa.DealParticipantId> {

    /**
     * Trouve toutes les participations d'un deal
     * @param dealUuid UUID du deal
     * @return Liste des participations
     */
    List<DealParticipantJpa> findByIdDealUuid(UUID dealUuid);

    /**
     * Trouve toutes les participations d'un utilisateur
     * @param utilisateurUuid UUID de l'utilisateur
     * @return Liste des participations
     */
    List<DealParticipantJpa> findByIdUtilisateurUuid(UUID utilisateurUuid);

    /**
     * Trouve une participation spécifique
     * @param dealUuid UUID du deal
     * @param utilisateurUuid UUID de l'utilisateur
     * @return Participation si elle existe
     */
    Optional<DealParticipantJpa> findByIdDealUuidAndIdUtilisateurUuid(UUID dealUuid, UUID utilisateurUuid);

    /**
     * Vérifie si un utilisateur participe déjà à un deal
     * @param dealUuid UUID du deal
     * @param utilisateurUuid UUID de l'utilisateur
     * @return true si l'utilisateur participe déjà
     */
    boolean existsByIdDealUuidAndIdUtilisateurUuid(UUID dealUuid, UUID utilisateurUuid);

    /**
     * Compte le nombre de participants d'un deal
     * @param dealUuid UUID du deal
     * @return Nombre de participants
     */
    long countByIdDealUuid(UUID dealUuid);

    /**
     * Supprime toutes les participations d'un deal
     * @param dealUuid UUID du deal
     */
    void deleteByIdDealUuid(UUID dealUuid);

    /**
     * Supprime toutes les participations d'un utilisateur
     * @param utilisateurUuid UUID de l'utilisateur
     */
    void deleteByIdUtilisateurUuid(UUID utilisateurUuid);
}

