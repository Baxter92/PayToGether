package com.ulr.paytogether.core.event;

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
 * Événement émis pour rappeler un paiement avant échéance
 */
@Getter
@NoArgsConstructor
public class PaymentReminderEvent extends DomainEvent {

    private UUID utilisateurUuid;
    private String email;
    private String prenom;
    private String nom;
    private BigDecimal montant;
    private String titreDeal;
    private LocalDateTime dateEcheance;
    private String lienPaiement;

    @JsonCreator
    public PaymentReminderEvent(
            @JsonProperty("utilisateurUuid") UUID utilisateurUuid,
            @JsonProperty("email") String email,
            @JsonProperty("prenom") String prenom,
            @JsonProperty("nom") String nom,
            @JsonProperty("montant") BigDecimal montant,
            @JsonProperty("titreDeal") String titreDeal,
            @JsonProperty("dateEcheance") LocalDateTime dateEcheance,
            @JsonProperty("lienPaiement") String lienPaiement) {
        super("PaiementService");
        this.utilisateurUuid = utilisateurUuid;
        this.email = email;
        this.prenom = prenom;
        this.nom = nom;
        this.montant = montant;
        this.titreDeal = titreDeal;
        this.dateEcheance = dateEcheance;
        this.lienPaiement = lienPaiement;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing PaymentReminderEvent", e);
        }
    }
}

