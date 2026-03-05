package com.ulr.paytogether.bff.event.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement émis pour notifier l'activation d'un compte utilisateur
 */
@Getter
@NoArgsConstructor
public class AccountActivationEvent extends DomainEvent {

    private UUID utilisateurUuid;
    private String email;
    private String prenom;
    private String nom;
    private LocalDateTime dateActivation;

    @JsonCreator
    public AccountActivationEvent(
            @JsonProperty("utilisateurUuid") UUID utilisateurUuid,
            @JsonProperty("email") String email,
            @JsonProperty("prenom") String prenom,
            @JsonProperty("nom") String nom,
            @JsonProperty("dateActivation") LocalDateTime dateActivation) {
        super("UtilisateurService");
        this.utilisateurUuid = utilisateurUuid;
        this.email = email;
        this.prenom = prenom;
        this.nom = nom;
        this.dateActivation = dateActivation;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing AccountActivationEvent", e);
        }
    }
}

