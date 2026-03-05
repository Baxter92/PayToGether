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
 * Entité JPA pour les tokens de validation (compte, mot de passe, etc.)
 */
@Entity
@Table(name = "validation_token", indexes = {
    @Index(name = "idx_token", columnList = "token", unique = true),
    @Index(name = "idx_utilisateur_uuid", columnList = "utilisateur_uuid")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationTokenJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "utilisateur_uuid", nullable = false)
    private UUID utilisateurUuid;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    @Column(name = "type_token", nullable = false, length = 50)
    private String typeToken; // VALIDATION_COMPTE, REINITIALISATION_MOT_DE_PASSE

    @Column(nullable = false)
    @Builder.Default
    private Boolean utilise = false;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
}

