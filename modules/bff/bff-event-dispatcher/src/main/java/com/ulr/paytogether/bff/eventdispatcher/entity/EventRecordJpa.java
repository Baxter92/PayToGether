package com.ulr.paytogether.bff.eventdispatcher.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA pour stocker les événements en base de données.
 * Chaque événement publié est enregistré avec son statut de consommation.
 */
@Entity
@Table(name = "event_record", indexes = {
    @Index(name = "idx_event_status", columnList = "status"),
    @Index(name = "idx_event_type", columnList = "eventType"),
    @Index(name = "idx_source_class", columnList = "sourceClass"),
    @Index(name = "idx_occurred_on", columnList = "occurredOn")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRecordJpa {

    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 255)
    private String eventType;

    @Column(name = "source_class", nullable = false, length = 255)
    private String sourceClass;

    @Column(name = "occurred_on", nullable = false)
    private LocalDateTime occurredOn;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private EventStatus status = EventStatus.PENDING;

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private Integer maxAttempts = 3;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "consumer_handler", length = 255)
    private String consumerHandler;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Enum pour le statut de l'événement
     */
    public enum EventStatus {
        PENDING,    // En attente de traitement
        PROCESSING, // En cours de traitement
        CONSUMED,   // Consommé avec succès
        FAILED      // Échec après max tentatives
    }
}

