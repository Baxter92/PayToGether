package com.ulr.paytogether.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Événement émis pour envoyer une notification de paiement.
 * Peut être traité par un service d'email ou SMS.
 */
@Getter
@NoArgsConstructor
public class PaymentNotificationEvent extends DomainEvent {

    private UUID utilisateurUuid;
    private UUID paiementUuid;
    private String email;
    private String sujetNotification;
    private String messageNotification;
    private String typeNotification; // EMAIL, SMS, PUSH

    @JsonCreator
    public PaymentNotificationEvent(
            @JsonProperty("utilisateurUuid") UUID utilisateurUuid,
            @JsonProperty("paiementUuid") UUID paiementUuid,
            @JsonProperty("email") String email,
            @JsonProperty("sujetNotification") String sujetNotification,
            @JsonProperty("messageNotification") String messageNotification,
            @JsonProperty("typeNotification") String typeNotification) {
        super("NotificationService");
        this.utilisateurUuid = utilisateurUuid;
        this.paiementUuid = paiementUuid;
        this.email = email;
        this.sujetNotification = sujetNotification;
        this.messageNotification = messageNotification;
        this.typeNotification = typeNotification;
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

