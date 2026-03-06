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
 * Événement émis pour notifier le marchand qu'un deal a été annulé
 */
@Getter
@NoArgsConstructor
@Builder
public class DealCancelledEvent extends DomainEvent {

    private UUID dealUuid;
    private UUID marchandUuid;
    private String emailMarchand;
    private String prenomMarchand;
    private String nomMarchand;
    private String titreDeal;
    private String raisonAnnulation;
    private LocalDateTime dateAnnulation;
    private String supportEmail;

    @JsonCreator
    public DealCancelledEvent(
            @JsonProperty("dealUuid") UUID dealUuid,
            @JsonProperty("marchandUuid") UUID marchandUuid,
            @JsonProperty("emailMarchand") String emailMarchand,
            @JsonProperty("prenomMarchand") String prenomMarchand,
            @JsonProperty("nomMarchand") String nomMarchand,
            @JsonProperty("titreDeal") String titreDeal,
            @JsonProperty("raisonAnnulation") String raisonAnnulation,
            @JsonProperty("dateAnnulation") LocalDateTime dateAnnulation,
            @JsonProperty("supportEmail") String supportEmail) {
        super("DealService");
        this.dealUuid = dealUuid;
        this.marchandUuid = marchandUuid;
        this.emailMarchand = emailMarchand;
        this.prenomMarchand = prenomMarchand;
        this.nomMarchand = nomMarchand;
        this.titreDeal = titreDeal;
        this.raisonAnnulation = raisonAnnulation;
        this.dateAnnulation = dateAnnulation;
        this.supportEmail = supportEmail;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing DealCancelledEvent", e);
        }
    }
}

