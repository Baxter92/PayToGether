package com.ulr.paytogether.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement émis lors de la mise à jour d'un deal
 * Permet de synchroniser l'index Elasticsearch
 */
@Getter
@NoArgsConstructor
@Builder
public class DealUpdatedEvent extends DomainEvent {

    private UUID dealUuid;
    private String titreDeal;
    private LocalDateTime dateModification;

    @JsonCreator
    public DealUpdatedEvent(
            @JsonProperty("dealUuid") UUID dealUuid,
            @JsonProperty("titreDeal") String titreDeal,
            @JsonProperty("dateModification") LocalDateTime dateModification) {
        super("DealService");
        this.dealUuid = dealUuid;
        this.titreDeal = titreDeal;
        this.dateModification = dateModification;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing DealUpdatedEvent", e);
        }
    }
}

