package com.ulr.paytogether.provider.adapter.entity;

import com.ulr.paytogether.provider.adapter.entity.enumeration.MethodePayout;
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
 * Entité JPA Payout (suffixe Jpa selon les instructions)
 */
@Entity
@Table(name = "payout")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false, length = 50)
    private String statut;

    @OneToOne
    @JoinColumn(name = "marchand_uuid", nullable = false)
    private UtilisateurJpa marchand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MethodePayout methodePayout;

    @Column(nullable = false)
    private LocalDateTime datePayout;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;
}
