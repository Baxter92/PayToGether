package com.ulr.paytogether.provider.adapter.entity;

import com.ulr.paytogether.core.enumeration.StatutImage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Entité JPA ImageDeal
 */
@Entity
@Table(name = "image_deal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDealJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String urlImage;

    @ManyToOne
    @JoinColumn(name = "deal_uuid", nullable = false)
    private DealJpa dealJpa;

    @Builder.Default
    private Boolean isPrincipal = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private StatutImage statut = StatutImage.PENDING;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;


    public void setUrlImageUnique(String urlImage) {
        this.urlImage = urlImage + "_" + new Date().getTime();
    }
}
