package com.ulr.paytogether.core.event;

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
 * Événement émis pour envoyer un email de validation de compte.
 * Contient toutes les informations nécessaires pour générer l'email personnalisé.
 */
@Getter
@NoArgsConstructor
public class AccountValidationEvent extends DomainEvent {

    private UUID utilisateurUuid;
    private String email;
    private String prenom;
    private String nom;
    private String token;
    private LocalDateTime dateExpiration;

    @JsonCreator
    public AccountValidationEvent(
            @JsonProperty("utilisateurUuid") UUID utilisateurUuid,
            @JsonProperty("email") String email,
            @JsonProperty("prenom") String prenom,
            @JsonProperty("nom") String nom,
            @JsonProperty("token") String token,
            @JsonProperty("dateExpiration") LocalDateTime dateExpiration) {
        super("UtilisateurService");
        this.utilisateurUuid = utilisateurUuid;
        this.email = email;
        this.prenom = prenom;
        this.nom = nom;
        this.token = token;
        this.dateExpiration = dateExpiration;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing AccountValidationEvent", e);
        }
    }
}

