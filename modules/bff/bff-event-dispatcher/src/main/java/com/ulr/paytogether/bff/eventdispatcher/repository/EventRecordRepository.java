package com.ulr.paytogether.bff.eventdispatcher.repository;

import com.ulr.paytogether.bff.eventdispatcher.entity.EventRecordJpa;
import com.ulr.paytogether.bff.eventdispatcher.entity.EventRecordJpa.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour la gestion des événements en base de données
 */
@Repository
public interface EventRecordRepository extends JpaRepository<EventRecordJpa, UUID> {

    /**
     * Trouve tous les événements en attente de traitement
     */
    List<EventRecordJpa> findByStatus(EventStatus status);

    /**
     * Trouve tous les événements avec pagination (pour admin)
     */
    Page<EventRecordJpa> findByStatus(EventStatus status, Pageable pageable);

    /**
     * Trouve un nombre limité d'événements PENDING, triés par date d'occurrence (les plus anciens en premier)
     * 
     * ⚠️ PROTECTION CRITIQUE : Limite le nombre d'événements traités par batch
     * pour éviter les boucles infinies et les 186 emails identiques !
     * 
     * Utilisation : findByStatusOrderByOccurredOnAsc(EventStatus.PENDING, PageRequest.of(0, 10))
     * 
     * @param status Le statut des événements à récupérer
     * @param pageable Pagination avec limite (ex: PageRequest.of(0, 10) pour 10 premiers)
     * @return Liste limitée d'événements triés par date
     */
    List<EventRecordJpa> findByStatusOrderByOccurredOnAsc(EventStatus status, Pageable pageable);

    /**
     * Trouve les événements en attente pour un type spécifique
     */
    List<EventRecordJpa> findByStatusAndEventType(EventStatus status, String eventType);

    /**
     * Claim atomique d'un événement : PENDING → PROCESSING uniquement si encore PENDING.
     * Retourne le nombre de lignes mises à jour (0 = déjà pris, 1 = succès).
     *
     * ✅ ANTI-DOUBLON : UPDATE atomique WHERE status='PENDING' protège contre multi-thread/multi-pod
     * ✅ clearAutomatically=true : vide le L1 cache JPA après l'UPDATE pour que findById()
     *    retourne l'état frais (PROCESSING) et non l'état stale du cache de session
     * ✅ Utilise les littéraux string 'PENDING'/'PROCESSING' (JPQL ne supporte pas les FQCN dans UPDATE)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE EventRecordJpa e SET e.status = 'PROCESSING', e.lastAttemptAt = :now, e.updatedAt = :now WHERE e.eventId = :id AND e.status = 'PENDING'")
    int claimForProcessing(@Param("id") UUID id, @Param("now") LocalDateTime now);

    /**
     * Trouve les événements bloqués (en traitement depuis trop longtemps)
     */
    @Query("SELECT e FROM EventRecordJpa e WHERE e.status = 'PROCESSING' AND e.lastAttemptAt < :threshold")
    List<EventRecordJpa> findStuckEvents(@Param("threshold") LocalDateTime threshold);

    /**
     * Met à jour le statut d'un événement
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE EventRecordJpa e SET e.status = :status, e.updatedAt = :updatedAt WHERE e.eventId = :eventId")
    void updateStatus(@Param("eventId") UUID eventId, @Param("status") EventStatus status, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Incrémente le nombre de tentatives
     */
    @Modifying
    @Query("UPDATE EventRecordJpa e SET e.attempts = e.attempts + 1, e.lastAttemptAt = :attemptAt, e.updatedAt = :updatedAt WHERE e.eventId = :eventId")
    void incrementAttempts(@Param("eventId") UUID eventId, @Param("attemptAt") LocalDateTime attemptAt, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Compte les événements par statut
     */
    long countByStatus(EventStatus status);

    /**
     * Trouve les événements d'une classe source spécifique
     */
    List<EventRecordJpa> findBySourceClass(String sourceClass);

    /**
     * Trouve les événements FAILED avant une date donnée (pour batch de retraitement)
     */
    @Query("SELECT e FROM EventRecordJpa e WHERE e.status = 'FAILED' AND e.failedAt < :threshold ORDER BY e.failedAt ASC")
    Page<EventRecordJpa> findFailedEventsBefore(@Param("threshold") LocalDateTime threshold, Pageable pageable);
}

