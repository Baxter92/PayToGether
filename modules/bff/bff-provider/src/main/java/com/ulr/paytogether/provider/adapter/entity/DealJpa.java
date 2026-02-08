package com.ulr.paytogether.provider.adapter.entity;

import com.ulr.paytogether.core.enumeration.StatutDeal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Entité JPA Deal (spécifique à la base de données)
 * Préfixe "Jpa" selon les instructions de l'architecture
 */
@Entity
@Table(name = "deal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixDeal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixPart;

    @Column(nullable = false)
    private Integer nbParticipants;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            orphanRemoval = true, mappedBy = "utilisateurJpa")
    @Builder.Default
    private Set<UtilisateurJpa> participants = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDeal statut;

    @OneToOne
    @JoinColumn(name = "utilisateur_uuid", nullable = false)
    private UtilisateurJpa marchandJpa;

    @OneToOne
    @JoinColumn(name = "categorie_uuid", nullable = false)
    private CategorieJpa categorieJpa;

    @OneToMany(mappedBy = "dealJpa", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ImageDealJpa> imageDealJpas = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "deal_points_forts", joinColumns = @JoinColumn(name = "deal_uuid"))
    @Column(name = "point_fort")
    @Builder.Default
    private List<String> listePointsForts = new ArrayList<>();

    @Column
    private LocalDateTime dateExpiration;

    @Column(length = 100)
    private String ville;

    @Column(length = 100)
    private String pays;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;
}
