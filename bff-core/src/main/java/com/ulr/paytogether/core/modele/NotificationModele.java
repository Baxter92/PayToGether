package com.ulr.paytogether.core.modele;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle métier Notification (indépendant de JPA)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationModele {

    private UUID uuid;
    private String titre;
    private String message;
    private Boolean lue;
    private UUID utilisateurUuid;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
