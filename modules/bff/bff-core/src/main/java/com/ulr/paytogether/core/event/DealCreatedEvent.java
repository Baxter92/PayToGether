package com.ulr.paytogether.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement émis pour notifier le marchand qu'un nouveau deal a été créé
 */
@Getter
@NoArgsConstructor
@Builder
public class DealCreatedEvent extends DomainEvent {

    private UUID dealUuid;
    private UUID marchandUuid;
    private String emailMarchand;
    private String prenomMarchand;
    private String nomMarchand;
    private String titreDeal;
    private String descriptionDeal;
    private BigDecimal montant;
    private BigDecimal montantPart;
    private LocalDateTime dateCreation;
    private Integer nbParticipants;

    @JsonCreator
    public DealCreatedEvent(
            @JsonProperty("dealUuid") UUID dealUuid,
            @JsonProperty("marchandUuid") UUID marchandUuid,
            @JsonProperty("emailMarchand") String emailMarchand,
            @JsonProperty("prenomMarchand") String prenomMarchand,
            @JsonProperty("nomMarchand") String nomMarchand,
            @JsonProperty("titreDeal") String titreDeal,
            @JsonProperty("descriptionDeal") String descriptionDeal,
            @JsonProperty("montant") BigDecimal montant,
            @JsonProperty("montantPart") BigDecimal montantPart,
            @JsonProperty("dateCreation") LocalDateTime dateCreation,
            @JsonProperty("nbParticipants") Integer nbParticipants) {
        super("DealService");
        this.dealUuid = dealUuid;
        this.marchandUuid = marchandUuid;
        this.emailMarchand = emailMarchand;
        this.prenomMarchand = prenomMarchand;
        this.nomMarchand = nomMarchand;
        this.titreDeal = titreDeal;
        this.descriptionDeal = descriptionDeal;
        this.montant = montant;
        this.montantPart = montantPart;
        this.dateCreation = dateCreation;
        this.nbParticipants = nbParticipants;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing DealCreatedEvent", e);
        }
    }
}

