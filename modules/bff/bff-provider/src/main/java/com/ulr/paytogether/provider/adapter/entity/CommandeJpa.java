package com.ulr.paytogether.provider.adapter.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ulr.paytogether.core.enumeration.StatutCommande;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entité JPA Commande
 */
@Entity
@Table(name = "commande")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Column(nullable = false, unique = true, length = 100)
    private String numeroCommande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCommande statut;

    @OneToOne
    @JoinColumn(name = "utilisateur_uuid", nullable = false)
    private UtilisateurJpa marchandJpa;

    @OneToOne
    @JoinColumn(name = "deal_uuid", nullable = false)
    private DealJpa dealJpa;

    @Column(nullable = false)
    private LocalDateTime dateCommande;

    @JsonIgnore
    @OneToMany(mappedBy = "commandeJpa", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PaiementJpa> paiements;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;
}
