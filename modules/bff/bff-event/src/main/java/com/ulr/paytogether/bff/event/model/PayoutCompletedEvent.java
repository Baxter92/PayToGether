package com.ulr.paytogether.bff.event.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement émis pour notifier le marchand d'un payout
 */
@Getter
@NoArgsConstructor
public class PayoutCompletedEvent extends DomainEvent {

    private UUID dealUuid;
    private UUID marchandUuid;
    private String emailMarchand;
    private String prenomMarchand;
    private String nomMarchand;
    private String titreDeal;
    private BigDecimal montantTotal;
    private BigDecimal fraisService;
    private BigDecimal montantNet;
    private LocalDateTime datePayout;

    @JsonCreator
    public PayoutCompletedEvent(
            @JsonProperty("dealUuid") UUID dealUuid,
            @JsonProperty("marchandUuid") UUID marchandUuid,
            @JsonProperty("emailMarchand") String emailMarchand,
            @JsonProperty("prenomMarchand") String prenomMarchand,
            @JsonProperty("nomMarchand") String nomMarchand,
            @JsonProperty("titreDeal") String titreDeal,
            @JsonProperty("montantTotal") BigDecimal montantTotal,
            @JsonProperty("fraisService") BigDecimal fraisService,
            @JsonProperty("montantNet") BigDecimal montantNet,
            @JsonProperty("datePayout") LocalDateTime datePayout) {
        super("PayoutService");
        this.dealUuid = dealUuid;
        this.marchandUuid = marchandUuid;
        this.emailMarchand = emailMarchand;
        this.prenomMarchand = prenomMarchand;
        this.nomMarchand = nomMarchand;
        this.titreDeal = titreDeal;
        this.montantTotal = montantTotal;
        this.fraisService = fraisService;
        this.montantNet = montantNet;
        this.datePayout = datePayout;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing PayoutCompletedEvent", e);
        }
    }
}

