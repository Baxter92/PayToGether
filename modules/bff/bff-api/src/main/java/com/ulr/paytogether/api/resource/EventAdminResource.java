package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.bff.eventdispatcher.entity.EventRecordJpa;
import com.ulr.paytogether.bff.eventdispatcher.repository.EventRecordRepository;
import com.ulr.paytogether.bff.eventdispatcher.consumer.EventConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Resource pour gérer les événements en échec (FAILED).
 * Permet de consulter, retraiter et gérer les événements qui ont échoué.
 *
 * ⚠️ ENDPOINTS ADMIN UNIQUEMENT
 */
@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
@Slf4j
public class EventAdminResource {

    private final EventRecordRepository eventRecordRepository;
    private final EventConsumerService eventConsumerService;

    /**
     * Lister tous les événements en échec (FAILED)
     * GET /api/admin/events/failed?page=0&size=20
     */
    @GetMapping("/failed")
    public ResponseEntity<Page<EventRecordJpa>> listFailedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "failedAt"));
        Page<EventRecordJpa> failedEvents = eventRecordRepository.findByStatus(
                EventRecordJpa.EventStatus.FAILED, pageRequest);

        log.info("Trouvé {} événements FAILED", failedEvents.getTotalElements());
        return ResponseEntity.ok(failedEvents);
    }

    /**
     * Lister tous les événements PERMANENTLY_FAILED
     * GET /api/admin/events/permanently-failed?page=0&size=20
     */
    @GetMapping("/permanently-failed")
    public ResponseEntity<Page<EventRecordJpa>> listPermanentlyFailedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "failedAt"));
        Page<EventRecordJpa> events = eventRecordRepository.findByStatus(
                EventRecordJpa.EventStatus.PERMANENTLY_FAILED, pageRequest);

        log.info("Trouvé {} événements PERMANENTLY_FAILED", events.getTotalElements());
        return ResponseEntity.ok(events);
    }

    /**
     * Statistiques des événements
     * GET /api/admin/events/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEventStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pending",          eventRecordRepository.countByStatus(EventRecordJpa.EventStatus.PENDING));
        stats.put("processing",       eventRecordRepository.countByStatus(EventRecordJpa.EventStatus.PROCESSING));
        stats.put("consumed",         eventRecordRepository.countByStatus(EventRecordJpa.EventStatus.CONSUMED));
        stats.put("failed",           eventRecordRepository.countByStatus(EventRecordJpa.EventStatus.FAILED));
        stats.put("permanentlyFailed",eventRecordRepository.countByStatus(EventRecordJpa.EventStatus.PERMANENTLY_FAILED));
        stats.put("total",            eventRecordRepository.count());
        return ResponseEntity.ok(stats);
    }

    /**
     * Retraiter UN événement en échec spécifique
     * POST /api/admin/events/{eventId}/retry
     *
     * ✅ Utilise @Modifying query (pas de conflit @Version)
     * ✅ Délègue au EventTransactionProcessor (REQUIRES_NEW) via EventConsumerService
     */
    @PostMapping("/{eventId}/retry")
    public ResponseEntity<Map<String, String>> retryFailedEvent(@PathVariable UUID eventId) {
        log.info("Retraitement manuel de l'événement {}", eventId);

        EventRecordJpa event = eventRecordRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé: " + eventId));

        if (event.getStatus() != EventRecordJpa.EventStatus.FAILED) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "L'événement n'est pas en statut FAILED",
                    "currentStatus", event.getStatus().name()
            ));
        }

        int nouveauRetryCount = event.getRetryCount() + 1;
        log.info("Retraitement #{} de l'événement {}", nouveauRetryCount, eventId);

        // ✅ @Modifying query → pas de conflit @Version (UPDATE direct sans entité gérée)
        eventRecordRepository.updateStatus(eventId, EventRecordJpa.EventStatus.PENDING, LocalDateTime.now());

        // Mettre à jour retryCount séparément (save simple sur l'objet déjà détaché du contexte)
        event.setRetryCount(nouveauRetryCount);
        event.setErrorMessage(null);
        event.setAttempts(0);
        eventRecordRepository.save(event);

        try {
            // ✅ Délègue à EventTransactionProcessor via retraiterEvenement (REQUIRES_NEW)
            boolean traite = eventConsumerService.retraiterEvenement(eventId);
            if (traite) {
                log.info("✅ Événement {} retraité avec succès (retry #{})", eventId, nouveauRetryCount);
                return ResponseEntity.ok(Map.of(
                        "message", "Événement retraité avec succès",
                        "retryCount", String.valueOf(nouveauRetryCount)
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "message", "Événement skippé (déjà en cours de traitement)",
                        "retryCount", String.valueOf(nouveauRetryCount)
                ));
            }
        } catch (Exception e) {
            log.error("❌ Échec du retraitement de l'événement {}: {}", eventId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Échec du retraitement: " + e.getMessage(),
                    "retryCount", String.valueOf(nouveauRetryCount),
                    "warning", "L'événement sera marqué PERMANENTLY_FAILED"
            ));
        }
    }

    /**
     * Retraiter TOUS les événements en échec
     * POST /api/admin/events/retry-all?limit=50
     *
     * ⚠️ Les événements qui échouent à nouveau → PERMANENTLY_FAILED
     */
    @PostMapping("/retry-all")
    public ResponseEntity<Map<String, Object>> retryAllFailedEvents(
            @RequestParam(defaultValue = "50") int limit) {

        log.warn("⚠️ Retraitement de TOUS les événements FAILED (limite: {})", limit);

        Page<EventRecordJpa> failedEvents = eventRecordRepository.findByStatus(
                EventRecordJpa.EventStatus.FAILED,
                PageRequest.of(0, limit));

        int successCount = 0;
        int failureCount = 0;
        int permanentlyFailedCount = 0;

        for (EventRecordJpa event : failedEvents.getContent()) {
            try {
                int nouveauRetryCount = event.getRetryCount() + 1;
                log.info("Retraitement #{} de l'événement {}", nouveauRetryCount, event.getEventId());

                // ✅ @Modifying query → pas de conflit @Version
                eventRecordRepository.updateStatus(
                        event.getEventId(), EventRecordJpa.EventStatus.PENDING, LocalDateTime.now());

                event.setRetryCount(nouveauRetryCount);
                event.setErrorMessage(null);
                event.setAttempts(0);
                eventRecordRepository.save(event);

                // ✅ REQUIRES_NEW via EventConsumerService → transaction isolée
                boolean traite = eventConsumerService.retraiterEvenement(event.getEventId());
                if (traite) {
                    successCount++;
                }

            } catch (Exception e) {
                failureCount++;
                EventRecordJpa updated = eventRecordRepository.findById(event.getEventId()).orElse(null);
                if (updated != null && updated.getStatus() == EventRecordJpa.EventStatus.PERMANENTLY_FAILED) {
                    permanentlyFailedCount++;
                    log.error("⛔ Événement {} → PERMANENTLY_FAILED", event.getEventId());
                } else {
                    log.error("❌ Échec retraitement {}: {}", event.getEventId(), e.getMessage());
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalProcessed", failedEvents.getContent().size());
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("permanentlyFailedCount", permanentlyFailedCount);
        result.put("message", "Retraitement terminé");
        if (permanentlyFailedCount > 0) {
            result.put("warning", permanentlyFailedCount + " événement(s) marqué(s) PERMANENTLY_FAILED");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Supprimer un événement spécifique
     * DELETE /api/admin/events/{eventId}
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Map<String, String>> deleteEvent(@PathVariable UUID eventId) {
        EventRecordJpa event = eventRecordRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé: " + eventId));

        eventRecordRepository.delete(event);
        log.info("✅ Événement {} supprimé", eventId);
        return ResponseEntity.ok(Map.of("message", "Événement supprimé avec succès"));
    }

    /**
     * Nettoyer les vieux événements CONSUMED
     * DELETE /api/admin/events/cleanup?daysOld=30
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldEvents(
            @RequestParam(defaultValue = "30") int daysOld) {
        // TODO: Implémenter la logique de nettoyage
        return ResponseEntity.ok(Map.of(
                "message", "Fonctionnalité en développement",
                "daysOld", daysOld
        ));
    }
}

