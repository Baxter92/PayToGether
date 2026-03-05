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
 * Événement émis pour notifier un paiement réussi
 */
@Getter
@NoArgsConstructor
public class PaymentSuccessfulNotificationEvent extends DomainEvent {

    private UUID utilisateurUuid;
    private String email;
    private String prenom;
    private String nom;
    private BigDecimal montant;
    private String methodePaiement;
    private String titreDeal;
    private String descriptionDeal;

    @JsonCreator
    public PaymentSuccessfulNotificationEvent(
            @JsonProperty("utilisateurUuid") UUID utilisateurUuid,
            @JsonProperty("email") String email,
            @JsonProperty("prenom") String prenom,
            @JsonProperty("nom") String nom,
            @JsonProperty("montant") BigDecimal montant,
            @JsonProperty("methodePaiement") String methodePaiement,
            @JsonProperty("titreDeal") String titreDeal,
            @JsonProperty("descriptionDeal") String descriptionDeal) {
        super("PaiementService");
        this.utilisateurUuid = utilisateurUuid;
        this.email = email;
        this.prenom = prenom;
        this.nom = nom;
        this.montant = montant;
        this.methodePaiement = methodePaiement;
        this.titreDeal = titreDeal;
        this.descriptionDeal = descriptionDeal;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing PaymentSuccessfulNotificationEvent", e);
        }
    }
}

