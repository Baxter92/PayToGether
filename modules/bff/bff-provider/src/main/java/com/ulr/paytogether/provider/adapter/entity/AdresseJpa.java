package com.ulr.paytogether.provider.adapter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA Adresse
 */
@Entity
@Table(name = "adresse")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdresseJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 255)
    private String rue;

    @Column(nullable = false, length = 100)
    private String ville;

    @Column(nullable = false, length = 20)
    private String codePostal;

    private String numeroPhone;

    private String appartement;

    @Builder.Default
    @Column(length = 100)
    private String province = "Alberta";

    @Builder.Default
    @Column(length = 100)
    private String pays = "Canada";

    @Builder.Default
    private boolean homeDelivery = false;

    @OneToOne
    @JoinColumn(name = "paiement_uuid", nullable = false)
    private PaiementJpa paiement;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;
}
