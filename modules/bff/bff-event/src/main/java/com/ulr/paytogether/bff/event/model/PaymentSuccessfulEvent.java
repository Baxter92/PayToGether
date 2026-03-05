package com.ulr.paytogether.bff.event.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Événement émis lors d'un paiement Square réussi.
 */
@Getter
@NoArgsConstructor
public class PaymentSuccessfulEvent extends DomainEvent {

    private UUID utilisateurUuid;
    private UUID commandeUuid;
    private UUID paiementUuid;
    private BigDecimal montant;
    private String methodePaiement;
    private String squarePaymentId;
    private String squareReceiptUrl;

    @JsonCreator
    public PaymentSuccessfulEvent(
            @JsonProperty("utilisateurUuid") UUID utilisateurUuid,
            @JsonProperty("commandeUuid") UUID commandeUuid,
            @JsonProperty("paiementUuid") UUID paiementUuid,
            @JsonProperty("montant") BigDecimal montant,
            @JsonProperty("methodePaiement") String methodePaiement,
            @JsonProperty("squarePaymentId") String squarePaymentId,
            @JsonProperty("squareReceiptUrl") String squareReceiptUrl) {
        super("SquarePaymentService");
        this.utilisateurUuid = utilisateurUuid;
        this.commandeUuid = commandeUuid;
        this.paiementUuid = paiementUuid;
        this.montant = montant;
        this.methodePaiement = methodePaiement;
        this.squarePaymentId = squarePaymentId;
        this.squareReceiptUrl = squareReceiptUrl;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing PaymentSuccessfulEvent", e);
        }
    }
}

