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
 * Entité JPA Commentaire (spécifique à la base de données)
 * Préfixe "Jpa" selon les instructions de l'architecture
 */
@Entity
@Table(name = "commentaire")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentaireJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(nullable = false)
    private Integer note;

    @ManyToOne
    @JoinColumn(name = "utilisateur_uuid", nullable = false)
    private UtilisateurJpa utilisateurJpa;

    @ManyToOne
    @JoinColumn(name = "deal_uuid", nullable = false)
    private DealJpa dealJpa;

    /**
     * Commentaire parent (null si c'est un commentaire racine)
     * Relation auto-référencée
     */
    @ManyToOne
    @JoinColumn(name = "commentaire_parent_uuid")
    private CommentaireJpa commentaireParentJpa;

    /**
     * Flag indiquant si la réponse est marquée comme pertinente
     * Utilisé uniquement pour les réponses (commentaireParentJpa != null)
     * Par défaut false
     */
    @Column(name = "est_pertinent", nullable = false)
    @Builder.Default
    private Boolean estPertinent = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;
}
