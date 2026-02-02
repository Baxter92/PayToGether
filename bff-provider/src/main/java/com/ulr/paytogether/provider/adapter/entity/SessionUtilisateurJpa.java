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
 * Entité JPA SessionUtilisateur (suffixe Jpa selon les instructions)
 */
@Entity
@Table(name = "session_utilisateur")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionUtilisateurJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @OneToOne
    @JoinColumn(name = "utilisateur_uuid", nullable = false)
    private UtilisateurJpa utilisateurJpa;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime dateExpiration;

    @Column(length = 45)
    private String adresseIp;

    @Column(length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;
}
