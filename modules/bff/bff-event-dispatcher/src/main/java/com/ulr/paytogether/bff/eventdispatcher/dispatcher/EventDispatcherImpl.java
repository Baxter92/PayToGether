package com.ulr.paytogether.bff.eventdispatcher.dispatcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ulr.paytogether.bff.event.model.DomainEvent;
import com.ulr.paytogether.bff.event.model.EventDispatcher;
import com.ulr.paytogether.bff.eventdispatcher.entity.EventRecordJpa;
import com.ulr.paytogether.bff.eventdispatcher.repository.EventRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du dispatcher d'événements.
 * Enregistre tous les événements en base de données pour traitement ultérieur.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventDispatcherImpl implements EventDispatcher {

    private final EventRecordRepository eventRecordRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public EventDispatcherImpl(EventRecordRepository eventRecordRepository) {
        this.eventRecordRepository = eventRecordRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dispatch(DomainEvent event) {
        log.info("Dispatching event: {} from class: {}", event.getEventType(), event.getSourceClass());

        try {
            String payload = serializeEvent(event);

            EventRecordJpa eventRecord = EventRecordJpa.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .sourceClass(event.getSourceClass())
                    .occurredOn(event.getOccurredOn())
                    .payload(payload)
                    .status(EventRecordJpa.EventStatus.PENDING)
                    .attempts(0)
                    .maxAttempts(3)
                    .build();

            eventRecordRepository.save(eventRecord);
            log.info("Event {} dispatched successfully", event.getEventId());

        } catch (Exception e) {
            log.error("Error dispatching event {}: {}", event.getEventId(), e.getMessage(), e);
            throw new RuntimeException("Failed to dispatch event", e);
        }
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void dispatchAsync(DomainEvent event) {
        log.info("Dispatching async event: {} from class: {}", event.getEventType(), event.getSourceClass());
        dispatch(event);
    }

    /**
     * Sérialise un événement en JSON
     */
    private String serializeEvent(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Error serializing event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}

