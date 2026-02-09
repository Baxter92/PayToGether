package com.ulr.paytogether.core.modele;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier SessionUtilisateur (indépendant de JPA)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionUtilisateurModele {

    private UUID uuid;
    private UUID utilisateurUuid;
    private String token;
    private LocalDateTime dateExpiration;
    private String adresseIp;
    private String userAgent;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
