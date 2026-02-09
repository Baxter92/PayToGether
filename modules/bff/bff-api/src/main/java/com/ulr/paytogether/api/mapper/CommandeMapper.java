package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.CommandeDTO;
import com.ulr.paytogether.api.dto.PaiementDTO;
import com.ulr.paytogether.core.modele.CommandeModele;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir entre CommandeModele (Core) et CommandeDTO (API)
 */
@Component
@RequiredArgsConstructor
public class CommandeMapper {

    private final PaiementMapper paiementMapper;

    /**
     * Convertit un modèle Core en DTO API
     */
    public CommandeDTO modeleVersDto(CommandeModele modele) {
        if (modele == null) {
            return null;
        }

        return CommandeDTO.builder()
                .uuid(modele.getUuid())
                .montantTotal(modele.getMontantTotal())
                .statut(modele.getStatut())
                .utilisateurUuid(modele.getUtilisateur() != null
                        ? modele.getUtilisateur().getUuid()
                        : null)
                .utilisateurNom(modele.getUtilisateur() != null
                        ? modele.getUtilisateur().getNom()
                        : null)
                .utilisateurPrenom(modele.getUtilisateur() != null
                        ? modele.getUtilisateur().getPrenom()
                        : null)
                .dealUuid(modele.getDealModele() != null
                        ? modele.getDealModele().getUuid()
                        : null)
                .dealTitre(modele.getDealModele() != null
                        ? modele.getDealModele().getTitre()
                        : null)
                .dealPrixPart(modele.getDealModele() != null
                        ? modele.getDealModele().getPrixPart()
                        : null)
                .paiements(modele.getPaiements() != null
                        ? modele.getPaiements().stream()
                                .map(paiementMapper::modeleVersDto)
                                .collect(Collectors.toList())
                        : Collections.emptyList())
                .dateCommande(modele.getDateCommande())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }

    /**
     * Convertit un DTO API en modèle Core
     */
    public CommandeModele dtoVersModele(CommandeDTO dto) {
        if (dto == null) {
            return null;
        }

        return CommandeModele.builder()
                .uuid(dto.getUuid())
                .montantTotal(dto.getMontantTotal())
                .statut(dto.getStatut())
                .paiements(dto.getPaiements() != null
                        ? dto.getPaiements().stream()
                                .map(paiementMapper::dtoVersModele)
                                .collect(Collectors.toList())
                        : Collections.emptyList())
                .dateCommande(dto.getDateCommande())
                .dateCreation(dto.getDateCreation())
                .dateModification(dto.getDateModification())
                .build();
        // Note: L'utilisateur et le deal seront définis séparément par le service
    }
}
