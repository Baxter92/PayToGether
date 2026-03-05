package com.ulr.paytogether.core.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe de base pour tous les événements du domaine.
 * Tous les événements doivent hériter de cette classe.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({
    // Les sous-types seront ajoutés automatiquement
})
public abstract class DomainEvent implements Serializable {

    /**
     * Identifiant unique de l'événement
     */
    private UUID eventId;

    /**
     * Date et heure de création de l'événement
     */
    private LocalDateTime occurredOn;

    /**
     * Nom de la classe qui a émis l'événement
     */
    private String sourceClass;

    /**
     * Type de l'événement
     */
    private String eventType;

    /**
     * Version de l'événement (pour gestion de compatibilité)
     */
    private Integer version;

    /**
     * Constructeur avec initialisation automatique
     */
    protected DomainEvent(String sourceClass) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
        this.sourceClass = sourceClass;
        this.eventType = this.getClass().getSimpleName();
        this.version = 1;
    }

    /**
     * Retourne une représentation JSON de l'événement
     */
    public abstract String toJson();
}

