package com.ulr.paytogether.bff.eventdispatcher.dispatcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ulr.paytogether.core.event.EventPublisher;
import com.ulr.paytogether.bff.eventdispatcher.entity.EventRecordJpa;
import com.ulr.paytogether.bff.eventdispatcher.repository.EventRecordRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implémentation du publisher d'événements (ADAPTATEUR - Partie droite).
 * Enregistre tous les événements en base de données pour traitement ultérieur.
 *
 * Cette classe implémente le port EventPublisher défini dans bff-core.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventDispatcherImpl implements EventPublisher {

    private final EventRecordRepository eventRecordRepository;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishSync(Object event) {
        log.info("Publishing sync event: {}", event.getClass().getSimpleName());

        try {
            String payload = serializeEvent(event);
            String eventType = event.getClass().getSimpleName();

            EventRecordJpa eventRecord = EventRecordJpa.builder()
                    .eventId(UUID.randomUUID())
                    .eventType(eventType)
                    .sourceClass(event.getClass().getName())
                    .occurredOn(LocalDateTime.now())
                    .payload(payload)
                    .status(EventRecordJpa.EventStatus.PENDING)
                    .attempts(0)
                    .maxAttempts(3)
                    .build();

            eventRecordRepository.save(eventRecord);
            log.info("Event {} published successfully", eventType);

        } catch (Exception e) {
            log.error("Error publishing event {}: {}", event.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void publishAsync(Object event) {
        log.info("Publishing async event: {}", event.getClass().getSimpleName());
        publishSync(event);
    }

    /**
     * Sérialise un événement en JSON
     */
    private String serializeEvent(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Error serializing event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}

