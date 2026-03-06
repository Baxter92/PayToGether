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
import java.util.UUID;

/**
 * Événement émis lors de l'initiation d'un paiement Square.
 */
@Builder
@NoArgsConstructor
@Getter
public class PaymentInitiatedEvent extends DomainEvent {

    private UUID utilisateurUuid;
    private UUID commandeUuid;
    private BigDecimal montant;
    private String methodePaiement;
    private String squareToken;

    @JsonCreator
    public PaymentInitiatedEvent(
            @JsonProperty("utilisateurUuid") UUID utilisateurUuid,
            @JsonProperty("commandeUuid") UUID commandeUuid,
            @JsonProperty("montant") BigDecimal montant,
            @JsonProperty("methodePaiement") String methodePaiement,
            @JsonProperty("squareToken") String squareToken) {
        super("SquarePaymentService");
        this.utilisateurUuid = utilisateurUuid;
        this.commandeUuid = commandeUuid;
        this.montant = montant;
        this.methodePaiement = methodePaiement;
        this.squareToken = squareToken;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing PaymentInitiatedEvent", e);
        }
    }
}

