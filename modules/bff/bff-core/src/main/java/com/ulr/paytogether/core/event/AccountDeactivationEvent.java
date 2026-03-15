package com.ulr.paytogether.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement émis pour notifier une désactivation de compte
 */
@Getter
@NoArgsConstructor
@Builder
public class AccountDeactivationEvent extends DomainEvent {

    private UUID utilisateurUuid;
    private String email;
    private String prenom;
    private String nom;
    private String raisonDesactivation;
    private LocalDateTime dateDesactivation;
    private String supportEmail;

    @JsonCreator
    public AccountDeactivationEvent(
            @JsonProperty("utilisateurUuid") UUID utilisateurUuid,
            @JsonProperty("email") String email,
            @JsonProperty("prenom") String prenom,
            @JsonProperty("nom") String nom,
            @JsonProperty("raisonDesactivation") String raisonDesactivation,
            @JsonProperty("dateDesactivation") LocalDateTime dateDesactivation,
            @JsonProperty("supportEmail") String supportEmail) {
        super("UtilisateurService");
        this.utilisateurUuid = utilisateurUuid;
        this.email = email;
        this.prenom = prenom;
        this.nom = nom;
        this.raisonDesactivation = raisonDesactivation;
        this.dateDesactivation = dateDesactivation;
        this.supportEmail = supportEmail;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing AccountDeactivationEvent", e);
        }
    }
}

