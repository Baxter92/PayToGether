package com.ulr.paytogether.bff.event.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Événement émis quand un handler a consommé avec succès un événement.
 * Cet événement DOIT être publié à la fin de chaque traitement réussi.
 */
@Getter
@NoArgsConstructor
public class HandlerConsumedEvent extends DomainEvent {

    /**
     * UUID de l'événement original qui a été consommé
     */
    private UUID originalEventId;

    /**
     * Type de l'événement original
     */
    private String originalEventType;

    /**
     * Nom du handler qui a consommé l'événement
     */
    private String handlerName;

    /**
     * Message de confirmation (optionnel)
     */
    private String message;

    /**
     * Données additionnelles (optionnel)
     */
    private String additionalData;

    @JsonCreator
    public HandlerConsumedEvent(
            @JsonProperty("originalEventId") UUID originalEventId,
            @JsonProperty("originalEventType") String originalEventType,
            @JsonProperty("handlerName") String handlerName,
            @JsonProperty("message") String message,
            @JsonProperty("additionalData") String additionalData) {
        super("HandlerExecutionTracker");
        this.originalEventId = originalEventId;
        this.originalEventType = originalEventType;
        this.handlerName = handlerName;
        this.message = message;
        this.additionalData = additionalData;
    }

    public HandlerConsumedEvent(UUID originalEventId, String originalEventType, String handlerName) {
        this(originalEventId, originalEventType, handlerName, null, null);
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing HandlerConsumedEvent", e);
        }
    }
}

