package com.ulr.paytogether.bff.eventdispatcher.repository;

import com.ulr.paytogether.bff.eventdispatcher.entity.EventRecordJpa;
import com.ulr.paytogether.bff.eventdispatcher.entity.EventRecordJpa.EventStatus;
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
     * Trouve les événements bloqués (en traitement depuis trop longtemps)
     */
    @Query("SELECT e FROM EventRecordJpa e WHERE e.status = 'PROCESSING' AND e.lastAttemptAt < :threshold")
    List<EventRecordJpa> findStuckEvents(@Param("threshold") LocalDateTime threshold);

    /**
     * Met à jour le statut d'un événement
     */
    @Modifying
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
}

