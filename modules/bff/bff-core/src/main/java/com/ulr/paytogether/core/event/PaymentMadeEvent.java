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
 * Événement émis lors d'un paiement effectué.
 * Exemple d'implémentation d'un événement du domaine.
 */
@Getter
@NoArgsConstructor
public class PaymentMadeEvent extends DomainEvent {

    private UUID utilisateurUuid;
    private UUID commandeUuid;
    private BigDecimal montant;
    private String methodePaiement;
    private String statut;

    @JsonCreator
    public PaymentMadeEvent(
            @JsonProperty("utilisateurUuid") UUID utilisateurUuid,
            @JsonProperty("commandeUuid") UUID commandeUuid,
            @JsonProperty("montant") BigDecimal montant,
            @JsonProperty("methodePaiement") String methodePaiement,
            @JsonProperty("statut") String statut) {
        super("PaymentService");
        this.utilisateurUuid = utilisateurUuid;
        this.commandeUuid = commandeUuid;
        this.montant = montant;
        this.methodePaiement = methodePaiement;
        this.statut = statut;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing PaymentMadeEvent", e);
        }
    }
}

