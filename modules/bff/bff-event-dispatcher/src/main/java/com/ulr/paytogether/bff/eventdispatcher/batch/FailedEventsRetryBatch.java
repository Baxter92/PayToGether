package com.ulr.paytogether.bff.eventdispatcher.batch;

import com.ulr.paytogether.bff.eventdispatcher.consumer.EventConsumerService;
import com.ulr.paytogether.bff.eventdispatcher.entity.EventRecordJpa;
import com.ulr.paytogether.bff.eventdispatcher.repository.EventRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Batch planifié pour retraiter automatiquement les événements en échec.
 *
 * ⚠️ DÉSACTIVÉ PAR DÉFAUT
 * Pour activer ce batch, ajouter dans application.properties :
 * events.retry.batch.enabled=true
 * events.retry.batch.olderThanHours=24  # Retraiter les événements échoués depuis plus de 24h
 * events.retry.batch.limit=50           # Limite de 50 événements par exécution
 *
 * ⚠️ EXCLUSIONS IMPORTANTES
 * Les événements de paiement sont EXCLUS du batch automatique :
 * - PaymentInitiatedEvent
 * - PaymentSuccessfulEvent
 * - PaymentFailedEvent
 *
 * Ces événements nécessitent un retraitement manuel via API admin pour éviter
 * les doublons (emails multiples, notifications, etc.)
 *
 * Exemple de configuration :
 * events.retry.batch.enabled=true
 * events.retry.batch.olderThanHours=24
 * events.retry.batch.limit=50
 *
 * Le batch s'exécute tous les jours à 3h du matin (cron: 0 0 3 * * ?)
 */
@Component
@ConditionalOnProperty(
    name = "events.retry.batch.enabled",
    havingValue = "true",
    matchIfMissing = false  // Désactivé par défaut
)
@RequiredArgsConstructor
@Slf4j
public class FailedEventsRetryBatch {

    private final EventRecordRepository eventRecordRepository;
    private final EventConsumerService eventConsumerService;

    /**
     * Retraite automatiquement les événements en échec.
     *
     * Exécution : Tous les jours à 3h du matin
     * Cron : 0 0 3 * * ? (seconde minute heure jour mois jourSemaine)
     *
     * Conditions de retraitement :
     * - Statut = FAILED
     * - Date d'échec > X heures (configurable)
     * - Limite de Y événements par exécution (configurable)
     */
    @Scheduled(cron = "${events.retry.batch.cron:0 0 3 * * ?}")
    public void retryFailedEvents() {
        log.info("========================================");
        log.info("🔄 Démarrage du batch de retraitement des événements en échec");
        log.info("========================================");

        try {
            int olderThanHours = Integer.parseInt(
                System.getProperty("events.retry.batch.olderThanHours", "24")
            );
            int limit = Integer.parseInt(
                System.getProperty("events.retry.batch.limit", "50")
            );

            log.info("Configuration:");
            log.info("  - Retraiter les événements échoués depuis plus de {} heures", olderThanHours);
            log.info("  - Limite : {} événements par exécution", limit);

            // Récupérer les événements FAILED de plus de X heures
            LocalDateTime threshold = LocalDateTime.now().minusHours(olderThanHours);
            Page<EventRecordJpa> failedEvents = eventRecordRepository
                .findFailedEventsBefore(threshold, PageRequest.of(0, limit));

            if (failedEvents.isEmpty()) {
                log.info("✅ Aucun événement en échec à retraiter");
                log.info("========================================");
                return;
            }

            log.info("Trouvé {} événements en échec à retraiter", failedEvents.getTotalElements());

            int successCount = 0;
            int failureCount = 0;
            int permanentlyFailedCount = 0;
            int skippedPaymentEvents = 0;

            // Types d'événements de paiement à exclure du batch automatique
            List<String> excludedPaymentEventTypes = Arrays.asList(
                "PaymentInitiatedEvent",
                "PaymentSuccessfulEvent",
                "PaymentFailedEvent"
            );

            for (EventRecordJpa event : failedEvents.getContent()) {
                try {
                    if (excludedPaymentEventTypes.contains(event.getEventType())) {
                        skippedPaymentEvents++;
                        log.warn("⚠️ Événement de paiement {} (type: {}) EXCLU du batch automatique",
                            event.getEventId(), event.getEventType());
                        log.info("   → Nécessite un retraitement manuel via API admin");
                        continue; // Passer au suivant
                    }

                    log.info("Retraitement de l'événement {} (type: {}, échec le: {}, retryCount actuel: {})",
                        event.getEventId(),
                        event.getEventType(),
                        event.getFailedAt(),
                        event.getRetryCount());

                    // ✅ Incrémenter retryCount pour tracker les retraitements
                    event.setRetryCount(event.getRetryCount() + 1);
                    log.info("Retraitement #{} de l'événement {}", event.getRetryCount(), event.getEventId());

                    // Réinitialiser l'événement à PENDING
                    event.setStatus(EventRecordJpa.EventStatus.PENDING);
                    event.setErrorMessage(null);
                    event.setAttempts(0);
                    eventRecordRepository.save(event);

                    // Traiter immédiatement l'événement
                    eventConsumerService.processEvent(event);

                    successCount++;
                    log.info("✅ Événement {} retraité avec succès", event.getEventId());

                } catch (Exception e) {
                    failureCount++;

                    // Vérifier si l'événement est maintenant PERMANENTLY_FAILED
                    EventRecordJpa updatedEvent = eventRecordRepository.findById(event.getEventId()).orElse(null);
                    if (updatedEvent != null && updatedEvent.getStatus() == EventRecordJpa.EventStatus.PERMANENTLY_FAILED) {
                        permanentlyFailedCount++;
                        log.error("⛔ Événement {} marqué PERMANENTLY_FAILED après échec du retraitement #{}",
                            event.getEventId(), updatedEvent.getRetryCount());
                    } else {
                        log.error("❌ Échec du retraitement de l'événement {}: {}",
                            event.getEventId(), e.getMessage(), e);
                    }
                }
            }

            log.info("========================================");
            log.info("Batch terminé:");
            log.info("  - Total traité : {}", failedEvents.getContent().size());
            log.info("  - Succès : {}", successCount);
            log.info("  - Échecs : {}", failureCount);
            log.info("  - Marqués PERMANENTLY_FAILED : {}", permanentlyFailedCount);
            log.info("  - Événements de paiement exclus : {}", skippedPaymentEvents);

            if (permanentlyFailedCount > 0) {
                log.warn("⚠️ {} événement(s) marqué(s) PERMANENTLY_FAILED - ne seront plus retraités",
                    permanentlyFailedCount);
            }

            if (skippedPaymentEvents > 0) {
                log.warn("⚠️ {} événement(s) de paiement EXCLUS du batch - nécessitent un retraitement manuel",
                    skippedPaymentEvents);
            }

            log.info("========================================");

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'exécution du batch de retraitement : {}",
                e.getMessage(), e);
        }
    }

    /**
     * Méthode utilitaire pour forcer l'exécution du batch manuellement.
     * Peut être appelée depuis un endpoint admin si nécessaire.
     */
    public void executeNow(int olderThanHours, int limit) {
        log.info("⚡ Exécution manuelle du batch (olderThanHours={}, limit={})",
            olderThanHours, limit);

        // Temporairement override les propriétés système
        String oldHours = System.getProperty("events.retry.batch.olderThanHours");
        String oldLimit = System.getProperty("events.retry.batch.limit");

        try {
            System.setProperty("events.retry.batch.olderThanHours", String.valueOf(olderThanHours));
            System.setProperty("events.retry.batch.limit", String.valueOf(limit));

            retryFailedEvents();

        } finally {
            // Restaurer les anciennes valeurs
            if (oldHours != null) {
                System.setProperty("events.retry.batch.olderThanHours", oldHours);
            } else {
                System.clearProperty("events.retry.batch.olderThanHours");
            }

            if (oldLimit != null) {
                System.setProperty("events.retry.batch.limit", oldLimit);
            } else {
                System.clearProperty("events.retry.batch.limit");
            }
        }
    }
}

