package com.ulr.paytogether.provider.adapter.entity;

import com.ulr.paytogether.core.enumeration.MethodePaiement;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA Paiement (spécifique à la base de données)
 * Préfixe "Jpa" selon les instructions de l'architecture
 * Support Square Payment avec champs additionnels
 */
@Entity
@Table(name = "paiement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPaiement statut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MethodePaiement methodePaiement;

    @Column(unique = true, length = 255)
    private String transactionId;

    // Champs spécifiques Square Payment
    @Column(length = 255)
    private String squarePaymentId;

    @Column(length = 255)
    private String squareOrderId;

    @Column(length = 255)
    private String squareLocationId;

    @Column(length = 500)
    private String squareReceiptUrl;

    @Column(length = 500)
    private String squareToken;

    @Column(length = 1000)
    private String messageErreur;

    @ManyToOne
    @JoinColumn(name = "utilisateur_uuid", nullable = false)
    private UtilisateurJpa utilisateurJpa;

    @ManyToOne
    @JoinColumn(name = "commande_uuid")
    private CommandeJpa commandeJpa;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime datePaiement = LocalDateTime.now();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;
}
