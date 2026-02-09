package com.ulr.paytogether.provider.adapter.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ulr.paytogether.core.enumeration.StatutImage;
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
 * Entité JPA Image
 */
@Entity
@Table(name = "image")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String urlImage;

    @Builder.Default
    private StatutImage statut = StatutImage.PENDING;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "publicite_uuid")
    private PubliciteJpa publiciteJpa;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;
}
