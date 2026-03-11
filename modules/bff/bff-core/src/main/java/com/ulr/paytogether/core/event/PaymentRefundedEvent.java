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
 * Événement émis lors du remboursement d'un paiement Square.
 * Déclenche l'envoi d'un email à l'utilisateur et la suppression de sa participation au deal.
 */
@Getter
@NoArgsConstructor
public class PaymentRefundedEvent extends DomainEvent {

    private UUID paiementUuid;
    private UUID utilisateurUuid;
    private UUID commandeUuid;
    private UUID dealUuid;
    private BigDecimal montantRembourse;
    private String refundId;
    private int nombreDeParts;

    // Informations pour l'email
    private String email;
    private String prenom;
    private String nom;
    private String titreDeal;
    private String descriptionDeal;
    private LocalDateTime dateRemboursement;
    private String raisonRemboursement;

    @JsonCreator
    public PaymentRefundedEvent(
            @JsonProperty("paiementUuid") UUID paiementUuid,
            @JsonProperty("utilisateurUuid") UUID utilisateurUuid,
            @JsonProperty("commandeUuid") UUID commandeUuid,
            @JsonProperty("dealUuid") UUID dealUuid,
            @JsonProperty("montantRembourse") BigDecimal montantRembourse,
            @JsonProperty("refundId") String refundId,
            @JsonProperty("nombreDeParts") int nombreDeParts,
            @JsonProperty("email") String email,
            @JsonProperty("prenom") String prenom,
            @JsonProperty("nom") String nom,
            @JsonProperty("titreDeal") String titreDeal,
            @JsonProperty("descriptionDeal") String descriptionDeal,
            @JsonProperty("dateRemboursement") LocalDateTime dateRemboursement,
            @JsonProperty("raisonRemboursement") String raisonRemboursement) {
        super("SquarePaymentRefundService");
        this.paiementUuid = paiementUuid;
        this.utilisateurUuid = utilisateurUuid;
        this.commandeUuid = commandeUuid;
        this.dealUuid = dealUuid;
        this.montantRembourse = montantRembourse;
        this.refundId = refundId;
        this.nombreDeParts = nombreDeParts;
        this.email = email;
        this.prenom = prenom;
        this.nom = nom;
        this.titreDeal = titreDeal;
        this.descriptionDeal = descriptionDeal;
        this.dateRemboursement = dateRemboursement;
        this.raisonRemboursement = raisonRemboursement;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur lors de la sérialisation de PaymentRefundedEvent", e);
        }
    }
}

