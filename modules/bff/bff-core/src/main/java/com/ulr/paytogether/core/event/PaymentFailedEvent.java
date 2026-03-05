package com.ulr.paytogether.core.event;

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
 * Événement émis lors d'un échec de paiement Square.
 */
@Getter
@NoArgsConstructor
public class PaymentFailedEvent extends DomainEvent {

    private UUID utilisateurUuid;
    private UUID commandeUuid;
    private UUID paiementUuid;
    private BigDecimal montant;
    private String methodePaiement;
    private String messageErreur;
    private String codeErreur;

    @JsonCreator
    public PaymentFailedEvent(
            @JsonProperty("utilisateurUuid") UUID utilisateurUuid,
            @JsonProperty("commandeUuid") UUID commandeUuid,
            @JsonProperty("paiementUuid") UUID paiementUuid,
            @JsonProperty("montant") BigDecimal montant,
            @JsonProperty("methodePaiement") String methodePaiement,
            @JsonProperty("messageErreur") String messageErreur,
            @JsonProperty("codeErreur") String codeErreur) {
        super("SquarePaymentService");
        this.utilisateurUuid = utilisateurUuid;
        this.commandeUuid = commandeUuid;
        this.paiementUuid = paiementUuid;
        this.montant = montant;
        this.methodePaiement = methodePaiement;
        this.messageErreur = messageErreur;
        this.codeErreur = codeErreur;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing PaymentFailedEvent", e);
        }
    }
}

