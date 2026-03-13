package com.ulr.paytogether.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeUtilisateurDto {
    private UUID uuid;
    private UUID commandeUuid;
    private UUID utilisateurUuid;
    private String nom;
    private String prenom;
    private String email;
    private String statutCommandeUtilisateur;
}
