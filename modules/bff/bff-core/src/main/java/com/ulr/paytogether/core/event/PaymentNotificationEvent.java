package com.ulr.paytogether.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement émis pour envoyer une notification de paiement.
 * Peut être traité par un service d'email ou SMS.
 */
@Getter
@NoArgsConstructor
@Builder
public class PaymentNotificationEvent extends DomainEvent {

    private UUID utilisateurUuid;
    private UUID paiementUuid;
    private String email;
    private String statutPaiement; // CONFIRMÉ, ÉCHOUÉ, EN_ATTENTE
    private String sujetNotification;
    private String messageNotification;
    private LocalDateTime datePaiement;
    private String methodePaiement;
    private String titreDeal;
    private String descriptionDeal;
    private BigDecimal montantPaiement;
    private String typeNotification; // EMAIL, SMS, PUSH

    @JsonCreator
    public PaymentNotificationEvent(
            @JsonProperty("utilisateurUuid") UUID utilisateurUuid,
            @JsonProperty("paiementUuid") UUID paiementUuid,
            @JsonProperty("email") String email,
            @JsonProperty("statutPaiement") String statutPaiement,
            @JsonProperty("sujetNotification") String sujetNotification,
            @JsonProperty("messageNotification") String messageNotification,
            @JsonProperty("datePaiement") LocalDateTime datePaiement,
            @JsonProperty("methodePaiement") String methodePaiement,
            @JsonProperty("titreDeal") String titreDeal,
            @JsonProperty("descriptionDeal") String descriptionDeal,
            @JsonProperty("montantPaiement") BigDecimal montantPaiement,
            @JsonProperty("typeNotification") String typeNotification) {
        super("NotificationService");
        this.utilisateurUuid = utilisateurUuid;
        this.paiementUuid = paiementUuid;
        this.email = email;
        this.statutPaiement = statutPaiement;
        this.sujetNotification = sujetNotification;
        this.messageNotification = messageNotification;
        this.datePaiement = datePaiement;
        this.methodePaiement = methodePaiement;
        this.typeNotification = typeNotification;
        this.titreDeal = titreDeal;
        this.descriptionDeal = descriptionDeal;
        this.montantPaiement = montantPaiement;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing PaymentNotificationEvent", e);
        }
    }
}

