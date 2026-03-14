package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.DealAvecStatutDTO;
import com.ulr.paytogether.api.dto.MarchandAvecDealsDTO;
import com.ulr.paytogether.core.modele.DealAvecStatutModele;
import com.ulr.paytogether.core.modele.MarchandAvecDealsModele;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir entre MarchandAvecDealsModele (Core) et MarchandAvecDealsDTO (API)
 */
@Component
public class MarchandAvecDealsMapper {

    /**
     * Convertit un modèle métier en DTO
     */
    public MarchandAvecDealsDTO modeleVersDto(MarchandAvecDealsModele modele) {
        if (modele == null) {
            return null;
        }

        List<DealAvecStatutDTO> dealsDto = modele.getDeals() != null
                ? modele.getDeals().stream()
                    .map(this::dealModeleVersDto)
                    .collect(Collectors.toList())
                : null;

        return MarchandAvecDealsDTO.builder()
                .uuid(modele.getUuid())
                .nom(modele.getNom())
                .prenom(modele.getPrenom())
                .email(modele.getEmail())
                .statut(modele.getStatut())
                .role(modele.getRole())
                .photoProfil(modele.getPhotoProfil())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .moyenneGlobale(modele.getMoyenneGlobale())
                .nombreDeals(modele.getNombreDeals())
                .deals(dealsDto)
                .build();
    }

    /**
     * Convertit un DealAvecStatutModele en DealAvecStatutDTO
     */
    private DealAvecStatutDTO dealModeleVersDto(DealAvecStatutModele modele) {
        if (modele == null) {
            return null;
        }

        return DealAvecStatutDTO.builder()
                .uuid(modele.getUuid())
                .titre(modele.getTitre())
                .description(modele.getDescription())
                .prixDeal(modele.getPrixDeal())
                .prixPart(modele.getPrixPart())
                .nbParticipants(modele.getNbParticipants())
                .dateDebut(modele.getDateDebut())
                .dateFin(modele.getDateFin())
                .statut(modele.getStatut())
                .ville(modele.getVille())
                .pays(modele.getPays())
                .dateCreation(modele.getDateCreation())
                .moyenneCommentaires(modele.getMoyenneCommentaires())
                .nombreParticipantsReel(modele.getNombreParticipantsReel())
                .nombrePartsAchetees(modele.getNombrePartsAchetees())
                .statutCommande(modele.getStatutCommande())
                .imageUrl(modele.getImageUrl())
                .build();
    }
}

