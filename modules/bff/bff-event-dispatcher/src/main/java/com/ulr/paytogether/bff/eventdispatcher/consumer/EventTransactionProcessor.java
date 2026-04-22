package com.ulr.paytogether.bff.eventdispatcher.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulr.paytogether.bff.eventdispatcher.entity.EventRecordJpa;
import com.ulr.paytogether.bff.eventdispatcher.repository.EventRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service dédié à l'exécution d'UN seul événement dans sa propre transaction isolée.
 *
 * ✅ POURQUOI un bean séparé ?
 * - Spring AOP ne proxifie pas les appels self (this.method()) → @Transactional ignoré
 * - Ce bean est injecté dans EventConsumerService → Spring intercepte l'appel → REQUIRES_NEW OK
 *
 * ✅ REQUIRES_NEW garantit :
 * - Chaque événement a sa propre transaction indépendante
 * - Un échec sur un événement ne rollback pas les autres
 * - Pas de conflit de version @Version entre événements du même batch
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventTransactionProcessor {

    private final EventRecordRepository eventRecordRepository;

    /**
     * Traite un événement dans une transaction ISOLATED (REQUIRES_NEW).
     *
     * ✅ CLAIM ATOMIQUE : UPDATE ... WHERE status = PENDING
     * - Si 0 lignes → déjà pris par un autre thread/pod → skip
     * - Si 1 ligne → on est propriétaire → continuer le traitement
     *
     * @param eventId          UUID de l'événement à traiter
     * @param handlers         liste des handlers compatibles (déjà calculée)
     * @param objectMapper     ObjectMapper pour désérialiser le payload
     * @return true si traité, false si skippé (déjà pris)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean processerEvenement(UUID eventId,
                                      List<HandlerRegistre> handlers,
                                      ObjectMapper objectMapper) {

        // ✅ CLAIM ATOMIQUE : protège contre les accès concurrents (multi-thread / multi-pod)
        // UPDATE event_record SET status='PROCESSING' WHERE id=? AND status='PENDING'
        int claimed = eventRecordRepository.claimForProcessing(eventId, LocalDateTime.now());

        if (claimed == 0) {
            log.warn("⏭️ Événement {} déjà pris par un autre thread/pod, skip", eventId);
            return false;
        }

        // Recharger depuis la BDD (état PROCESSING garanti, version fraîche)
        EventRecordJpa evenement = eventRecordRepository.findById(eventId).orElse(null);
        if (evenement == null) {
            log.warn("⚠️ Événement {} introuvable après claim, skip", eventId);
            return false;
        }

        try {
            // Désérialiser le payload
            Object event = deserialiserPayload(evenement, objectMapper);

            // Exécuter tous les handlers compatibles
            boolean tousSucces = true;
            StringBuilder erreurs = new StringBuilder();

            for (HandlerRegistre handler : handlers) {
                try {
                    executerHandler(handler, event, evenement);
                } catch (Exception e) {
                    tousSucces = false;
                    erreurs.append(handler.getNomHandler())
                           .append(": ").append(e.getMessage()).append("; ");
                    log.error("❌ Erreur handler {} pour événement {}: {}",
                            handler.getNomHandler(), eventId, e.getMessage(), e);
                }
            }

            if (tousSucces) {
                marquerConsomme(evenement, handlers.get(0).getNomHandler());
            } else {
                gererEchec(evenement, erreurs.toString());
            }

        } catch (Exception e) {
            log.error("❌ Erreur traitement événement {}: {}", eventId, e.getMessage(), e);
            gererEchec(evenement, e.getMessage());
        }

        return true;
    }

    /**
     * Réinitialise les événements bloqués dans sa propre transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reinitialiserEvenementsBloques() {
        LocalDateTime seuil = LocalDateTime.now().minusMinutes(15);
        List<EventRecordJpa> evenementsBloques = eventRecordRepository.findStuckEvents(seuil);

        if (!evenementsBloques.isEmpty()) {
            log.warn("⚠️ {} événements bloqués (PROCESSING > 15 min), réinitialisation → PENDING",
                    evenementsBloques.size());

            for (EventRecordJpa evt : evenementsBloques) {
                log.warn("⚠️ Réinit événement bloqué: {} type={} tentative={}/{} dernier={} ",
                        evt.getEventId(), evt.getEventType(),
                        evt.getAttempts(), evt.getMaxAttempts(),
                        evt.getLastAttemptAt());

                // Utiliser @Modifying query pour éviter conflit @Version
                eventRecordRepository.updateStatus(
                        evt.getEventId(),
                        EventRecordJpa.EventStatus.PENDING,
                        LocalDateTime.now());
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Méthodes privées
    // ────────────────────────────────────────────────────────────────────────────

    private Object deserialiserPayload(EventRecordJpa evenement, ObjectMapper objectMapper) throws Exception {
        String corePackage = "com.ulr.paytogether.core.event";
        try {
            Class<?> classeEvent = Class.forName(corePackage + "." + evenement.getEventType());
            return objectMapper.readValue(evenement.getPayload(), classeEvent);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException(
                    "Classe événement non trouvée: " + evenement.getEventType() + " dans " + corePackage);
        }
    }

    private void executerHandler(HandlerRegistre handler, Object event, EventRecordJpa evenement)
            throws Exception {
        Method methode = handler.getMethode();
        methode.setAccessible(true);
        methode.invoke(handler.getInstanceHandler(), event);
        evenement.setConsumerHandler(handler.getNomHandler());

        log.info("✅ Handler {} a consommé l'événement {} (type={})",
                handler.getNomHandler(), evenement.getEventId(), evenement.getEventType());
    }

    private void marquerConsomme(EventRecordJpa evenement, String nomHandler) {
        // ✅ @Modifying query : pas de conflit @Version (UPDATE direct sans passer par l'entité gérée)
        eventRecordRepository.updateStatus(
                evenement.getEventId(),
                EventRecordJpa.EventStatus.CONSUMED,
                LocalDateTime.now());

        log.info("✅ Événement {} marqué CONSUMED par {}", evenement.getEventId(), nomHandler);
    }

    private void gererEchec(EventRecordJpa evenement, String messageErreur) {
        evenement.setAttempts(evenement.getAttempts() + 1);

        boolean estUnRetry = evenement.getRetryCount() > 0;

        if (estUnRetry) {
            // Après retraitement manuel → PERMANENTLY_FAILED
            log.error("❌ Échec APRÈS RETRY pour événement {} (retryCount={}, tentative {}): {}",
                    evenement.getEventId(), evenement.getRetryCount(),
                    evenement.getAttempts(), messageErreur);

            eventRecordRepository.updateStatus(
                    evenement.getEventId(),
                    EventRecordJpa.EventStatus.PERMANENTLY_FAILED,
                    LocalDateTime.now());

            log.warn("⛔ Événement {} → PERMANENTLY_FAILED", evenement.getEventId());
        } else {
            // Premier échec → FAILED (retraitable manuellement)
            log.error("❌ Échec pour événement {} (tentative {}): {}",
                    evenement.getEventId(), evenement.getAttempts(), messageErreur);

            eventRecordRepository.updateStatus(
                    evenement.getEventId(),
                    EventRecordJpa.EventStatus.FAILED,
                    LocalDateTime.now());

            log.warn("⚠️ Événement {} → FAILED (retraitement manuel possible)", evenement.getEventId());
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // DTO interne pour les handlers enregistrés
    // ────────────────────────────────────────────────────────────────────────────

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class HandlerRegistre {
        private Object instanceHandler;
        private Method methode;
        private Class<?> typeEvent;
        private int maxTentatives;
        private String nomHandler;
    }
}

