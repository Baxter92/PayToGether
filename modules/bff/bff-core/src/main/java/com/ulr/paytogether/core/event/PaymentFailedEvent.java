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
 * Publié UNIQUEMENT après épuisement de tous les max attempts.
 * Déclenche l'envoi d'un email d'échec à l'utilisateur.
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

    // Informations pour l'email
    private String email;
    private String prenom;
    private String nom;
    private String titreDeal;
    private String descriptionDeal;
    private int nombreDePart;
    private int nombreTentatives;

    // Adresse de livraison
    private String adresseRue;
    private String adresseVille;
    private String adresseProvince;
    private String adresseCodePostal;
    private String adressePays;

    @JsonCreator
    public PaymentFailedEvent(
            @JsonProperty("utilisateurUuid") UUID utilisateurUuid,
            @JsonProperty("commandeUuid") UUID commandeUuid,
            @JsonProperty("paiementUuid") UUID paiementUuid,
            @JsonProperty("montant") BigDecimal montant,
            @JsonProperty("methodePaiement") String methodePaiement,
            @JsonProperty("messageErreur") String messageErreur,
            @JsonProperty("codeErreur") String codeErreur,
            @JsonProperty("email") String email,
            @JsonProperty("prenom") String prenom,
            @JsonProperty("nom") String nom,
            @JsonProperty("titreDeal") String titreDeal,
            @JsonProperty("descriptionDeal") String descriptionDeal,
            @JsonProperty("nombreDePart") int nombreDePart,
            @JsonProperty("nombreTentatives") int nombreTentatives,
            @JsonProperty("adresseRue") String adresseRue,
            @JsonProperty("adresseVille") String adresseVille,
            @JsonProperty("adresseProvince") String adresseProvince,
            @JsonProperty("adresseCodePostal") String adresseCodePostal,
            @JsonProperty("adressePays") String adressePays) {
        super("SquarePaymentService");
        this.utilisateurUuid = utilisateurUuid;
        this.commandeUuid = commandeUuid;
        this.paiementUuid = paiementUuid;
        this.montant = montant;
        this.methodePaiement = methodePaiement;
        this.messageErreur = messageErreur;
        this.codeErreur = codeErreur;
        this.email = email;
        this.prenom = prenom;
        this.nom = nom;
        this.titreDeal = titreDeal;
        this.descriptionDeal = descriptionDeal;
        this.nombreDePart = nombreDePart;
        this.nombreTentatives = nombreTentatives;
        this.adresseRue = adresseRue;
        this.adresseVille = adresseVille;
        this.adresseProvince = adresseProvince;
        this.adresseCodePostal = adresseCodePostal;
        this.adressePays = adressePays;
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

