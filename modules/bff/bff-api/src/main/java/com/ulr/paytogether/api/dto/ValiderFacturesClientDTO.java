package com.ulr.paytogether.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO pour valider les factures des clients
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValiderFacturesClientDTO {
    private List<UUID> utilisateurUuids; // Liste des UUIDs des utilisateurs validés
}

