package com.ulr.paytogether.provider.adapter.entity;

import com.ulr.paytogether.core.enumeration.StatutCommandeUtilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Table de jointure Commande <-> Utilisateur avec colonne supplémentaire.
 *
 * But métier (prochaine étape) : stocker le statut de validation d'un utilisateur
 * (ex: validation par le marchand via numéro de paiement).
 */
@Entity
@Table(name = "commande_utilisateur")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeUtilisateurJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_uuid", nullable = false)
    private CommandeJpa commandeJpa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_uuid", nullable = false)
    private UtilisateurJpa utilisateurJpa;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_commande_utilisateur", nullable = false, length = 30)
    private StatutCommandeUtilisateur statutCommandeUtilisateur = StatutCommandeUtilisateur.EN_ATTENTE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;
}

