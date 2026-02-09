package com.ulr.paytogether.provider.adapter.entity;

import com.ulr.paytogether.core.enumeration.RoleUtilisateur;
import com.ulr.paytogether.core.enumeration.StatutUtilisateur;
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
 * Entité JPA Utilisateur (spécifique à la base de données)
 * Préfixe "Jpa" selon les instructions de l'architecture
 */
@Entity
@Table(name = "utilisateur")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(length = 100)
    private String nom;

    @Column(length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutUtilisateur statut = StatutUtilisateur.INACTIF;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleUtilisateur role = RoleUtilisateur.UTILISATEUR;

    @OneToOne
    private ImageUtilisateurJpa photoProfil;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;

    public void setPhotoProfilUnique(String urlImage) {
        if (this.photoProfil != null) {
            this.photoProfil.setUrlImage(urlImage+'_'+System.currentTimeMillis());
        }
    }

}
