package com.ulr.paytogether.provider.adapter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA pour la relation ManyToMany entre Deal et Utilisateur
 * avec attributs supplémentaires (nombreDePart)
 */
@Entity
@Table(name = "deal_participants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealParticipantJpa {

    @EmbeddedId
    private DealParticipantId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("dealUuid")
    @JoinColumn(name = "deal_uuid", nullable = false)
    private DealJpa dealJpa;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("utilisateurUuid")
    @JoinColumn(name = "utilisateur_uuid", nullable = false)
    private UtilisateurJpa utilisateurJpa;

    @Column(name = "nombre_de_part", nullable = false)
    @Builder.Default
    private Integer nombreDePart = 1;

    @CreationTimestamp
    @Column(name = "date_participation", nullable = false, updatable = false)
    private LocalDateTime dateParticipation;

    @UpdateTimestamp
    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    /**
     * Classe embarquée pour la clé composite
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DealParticipantId implements Serializable {

        @Column(name = "deal_uuid")
        private UUID dealUuid;

        @Column(name = "utilisateur_uuid")
        private UUID utilisateurUuid;
    }
}

