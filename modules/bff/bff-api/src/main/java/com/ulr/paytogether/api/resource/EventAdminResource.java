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

import java.util.HashMap;
import java.util.List;
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
     *
     * GET /api/admin/events/failed?page=0&size=20
     */
    @GetMapping("/failed")
    public ResponseEntity<Page<EventRecordJpa>> listFailedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Récupération des événements FAILED (page={}, size={})", page, size);

        PageRequest pageRequest = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "failedAt")
        );

        Page<EventRecordJpa> failedEvents = eventRecordRepository.findByStatus(
            EventRecordJpa.EventStatus.FAILED,
            pageRequest
        );

        log.info("Trouvé {} événements FAILED sur {} total",
            failedEvents.getNumberOfElements(),
            failedEvents.getTotalElements()
        );

        return ResponseEntity.ok(failedEvents);
    }

    /**
     * Lister tous les événements en échec permanent (PERMANENTLY_FAILED)
     *
     * GET /api/admin/events/permanently-failed?page=0&size=20
     *
     * Ces événements ne seront JAMAIS retraités automatiquement.
     * Ils ont échoué après au moins un retraitement manuel.
     */
    @GetMapping("/permanently-failed")
    public ResponseEntity<Page<EventRecordJpa>> listPermanentlyFailedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Récupération des événements PERMANENTLY_FAILED (page={}, size={})", page, size);

        PageRequest pageRequest = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "failedAt")
        );

        Page<EventRecordJpa> permanentlyFailedEvents = eventRecordRepository.findByStatus(
            EventRecordJpa.EventStatus.PERMANENTLY_FAILED,
            pageRequest
        );

        log.info("Trouvé {} événements PERMANENTLY_FAILED sur {} total",
            permanentlyFailedEvents.getNumberOfElements(),
            permanentlyFailedEvents.getTotalElements()
        );

        return ResponseEntity.ok(permanentlyFailedEvents);
    }

    /**
     * Récupérer les statistiques des événements
     *
     * GET /api/admin/events/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEventStats() {
        log.info("Récupération des statistiques des événements");

        Map<String, Object> stats = new HashMap<>();

        long pendingCount = eventRecordRepository.countByStatus(EventRecordJpa.EventStatus.PENDING);
        long processingCount = eventRecordRepository.countByStatus(EventRecordJpa.EventStatus.PROCESSING);
        long consumedCount = eventRecordRepository.countByStatus(EventRecordJpa.EventStatus.CONSUMED);
        long failedCount = eventRecordRepository.countByStatus(EventRecordJpa.EventStatus.FAILED);
        long permanentlyFailedCount = eventRecordRepository.countByStatus(EventRecordJpa.EventStatus.PERMANENTLY_FAILED);
        long totalCount = eventRecordRepository.count();

        stats.put("pending", pendingCount);
        stats.put("processing", processingCount);
        stats.put("consumed", consumedCount);
        stats.put("failed", failedCount);
        stats.put("permanentlyFailed", permanentlyFailedCount);
        stats.put("total", totalCount);

        log.info("Statistiques: PENDING={}, PROCESSING={}, CONSUMED={}, FAILED={}, PERMANENTLY_FAILED={}, TOTAL={}",
            pendingCount, processingCount, consumedCount, failedCount, permanentlyFailedCount, totalCount);

        return ResponseEntity.ok(stats);
    }

    /**
     * Retraiter UN événement en échec spécifique
     *
     * POST /api/admin/events/{eventId}/retry
     *
     * ⚠️ IMPORTANT :
     * - Si l'événement échoue à nouveau après ce retraitement,
     *   il sera marqué PERMANENTLY_FAILED et ne pourra plus être retraité automatiquement
     */
    @PostMapping("/{eventId}/retry")
    public ResponseEntity<Map<String, String>> retryFailedEvent(@PathVariable UUID eventId) {
        log.info("Tentative de retraitement de l'événement {}", eventId);

        EventRecordJpa event = eventRecordRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé: " + eventId));

        if (event.getStatus() != EventRecordJpa.EventStatus.FAILED) {
            log.warn("L'événement {} n'est pas en statut FAILED (statut actuel: {})", eventId, event.getStatus());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "L'événement n'est pas en statut FAILED",
                    "currentStatus", event.getStatus().name()
                ));
        }

        // ✅ Incrémenter retryCount pour tracker les retraitements manuels
        event.setRetryCount(event.getRetryCount() + 1);
        log.info("Retraitement #{} de l'événement {}", event.getRetryCount(), eventId);

        // Réinitialiser l'événement à PENDING pour être retraité
        event.setStatus(EventRecordJpa.EventStatus.PENDING);
        event.setErrorMessage(null);
        event.setAttempts(0);  // Réinitialiser les tentatives
        eventRecordRepository.save(event);

        log.info("✅ Événement {} réinitialisé à PENDING pour retraitement #{}", eventId, event.getRetryCount());

        // Traiter immédiatement l'événement
        try {
            eventConsumerService.processEvent(event);
            log.info("✅ Événement {} retraité avec succès", eventId);
            return ResponseEntity.ok(Map.of(
                "message", "Événement retraité avec succès",
                "retryCount", String.valueOf(event.getRetryCount())
            ));
        } catch (Exception e) {
            log.error("❌ Échec du retraitement de l'événement {}: {}", eventId, e.getMessage());
            return ResponseEntity.status(500)
                .body(Map.of(
                    "error", "Échec du retraitement: " + e.getMessage(),
                    "retryCount", String.valueOf(event.getRetryCount()),
                    "warning", "L'événement sera marqué PERMANENTLY_FAILED"
                ));
        }
    }

    /**
     * Retraiter TOUS les événements en échec
     *
     * POST /api/admin/events/retry-all?limit=50
     *
     * ⚠️ À utiliser avec précaution !
     * ⚠️ Les événements qui échouent à nouveau seront marqués PERMANENTLY_FAILED
     */
    @PostMapping("/retry-all")
    public ResponseEntity<Map<String, Object>> retryAllFailedEvents(
            @RequestParam(defaultValue = "50") int limit
    ) {
        log.warn("⚠️ Retraitement de TOUS les événements FAILED (limite: {})", limit);

        PageRequest pageRequest = PageRequest.of(0, limit);
        Page<EventRecordJpa> failedEvents = eventRecordRepository.findByStatus(
            EventRecordJpa.EventStatus.FAILED,
            pageRequest
        );

        int successCount = 0;
        int failureCount = 0;
        int permanentlyFailedCount = 0;

        for (EventRecordJpa event : failedEvents.getContent()) {
            try {
                // ✅ Incrémenter retryCount
                event.setRetryCount(event.getRetryCount() + 1);
                log.info("Retraitement #{} de l'événement {}", event.getRetryCount(), event.getEventId());

                // Réinitialiser à PENDING
                event.setStatus(EventRecordJpa.EventStatus.PENDING);
                event.setErrorMessage(null);
                event.setAttempts(0);
                eventRecordRepository.save(event);

                // Traiter immédiatement
                eventConsumerService.processEvent(event);
                successCount++;
                log.info("✅ Événement {} retraité avec succès", event.getEventId());
            } catch (Exception e) {
                failureCount++;

                // Vérifier si l'événement est maintenant PERMANENTLY_FAILED
                EventRecordJpa updatedEvent = eventRecordRepository.findById(event.getEventId()).orElse(null);
                if (updatedEvent != null && updatedEvent.getStatus() == EventRecordJpa.EventStatus.PERMANENTLY_FAILED) {
                    permanentlyFailedCount++;
                    log.error("⛔ Événement {} marqué PERMANENTLY_FAILED après échec du retraitement", event.getEventId());
                } else {
                    log.error("❌ Échec du retraitement de l'événement {}: {}", event.getEventId(), e.getMessage());
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

        log.info("Retraitement terminé: {} succès, {} échecs ({} permanently failed) sur {} événements",
            successCount, failureCount, permanentlyFailedCount, failedEvents.getContent().size());

        return ResponseEntity.ok(result);
    }

    /**
     * Supprimer un événement en échec (cleanup)
     *
     * DELETE /api/admin/events/{eventId}
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Map<String, String>> deleteEvent(@PathVariable UUID eventId) {
        log.warn("Suppression de l'événement {}", eventId);

        EventRecordJpa event = eventRecordRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé: " + eventId));

        eventRecordRepository.delete(event);

        log.info("✅ Événement {} supprimé", eventId);

        return ResponseEntity.ok(Map.of("message", "Événement supprimé avec succès"));
    }

    /**
     * Supprimer TOUS les événements CONSUMED de plus de X jours (cleanup)
     *
     * DELETE /api/admin/events/cleanup?daysOld=30
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldEvents(
            @RequestParam(defaultValue = "30") int daysOld
    ) {
        log.warn("⚠️ Nettoyage des événements CONSUMED de plus de {} jours", daysOld);

        // TODO: Implémenter la logique de nettoyage
        // Pour l'instant, retourner un message

        return ResponseEntity.ok(Map.of(
            "message", "Fonctionnalité en développement",
            "daysOld", daysOld
        ));
    }
}

