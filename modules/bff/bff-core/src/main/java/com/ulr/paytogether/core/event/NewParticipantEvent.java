package com.ulr.paytogether.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement émis pour notifier le marchand d'un nouveau participant à son deal
 */
@Getter
@NoArgsConstructor
public class NewParticipantEvent extends DomainEvent {

    private UUID dealUuid;
    private UUID marchandUuid;
    private String emailMarchand;
    private String prenomMarchand;
    private String nomMarchand;
    private String titreDeal;
    private String prenomParticipant;
    private String nomParticipant;
    private LocalDateTime dateParticipation;

    @JsonCreator
    public NewParticipantEvent(
            @JsonProperty("dealUuid") UUID dealUuid,
            @JsonProperty("marchandUuid") UUID marchandUuid,
            @JsonProperty("emailMarchand") String emailMarchand,
            @JsonProperty("prenomMarchand") String prenomMarchand,
            @JsonProperty("nomMarchand") String nomMarchand,
            @JsonProperty("titreDeal") String titreDeal,
            @JsonProperty("prenomParticipant") String prenomParticipant,
            @JsonProperty("nomParticipant") String nomParticipant,
            @JsonProperty("dateParticipation") LocalDateTime dateParticipation) {
        super("CommandeService");
        this.dealUuid = dealUuid;
        this.marchandUuid = marchandUuid;
        this.emailMarchand = emailMarchand;
        this.prenomMarchand = prenomMarchand;
        this.nomMarchand = nomMarchand;
        this.titreDeal = titreDeal;
        this.prenomParticipant = prenomParticipant;
        this.nomParticipant = nomParticipant;
        this.dateParticipation = dateParticipation;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing NewParticipantEvent", e);
        }
    }
}

