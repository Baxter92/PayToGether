package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.CommentaireDTO;
import com.ulr.paytogether.api.mapper.CommentaireMapper;
import com.ulr.paytogether.core.domaine.service.CommentaireService;
import com.ulr.paytogether.core.modele.CommentaireModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur API pour les commentaires
 * Fait le pont entre les contrôleurs REST et les services métier
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CommentaireApiAdapter {

    private final CommentaireService commentaireService;
    private final CommentaireMapper mapper;

    /**
     * Créer un nouveau commentaire
     */
    public CommentaireDTO creer(CommentaireDTO dto) {
        log.debug("Création d'un commentaire pour le deal: {}", dto.getDealUuid());

        CommentaireModele modele = mapper.dtoVersModele(dto);
        CommentaireModele cree = commentaireService.creer(modele);

        return mapper.modeleVersDto(cree);
    }

    /**
     * Trouver un commentaire par son UUID
     */
    public Optional<CommentaireDTO> trouverParUuid(UUID uuid) {
        log.debug("Recherche du commentaire: {}", uuid);

        return commentaireService.lireParUuid(uuid)
                .map(mapper::modeleVersDto);
    }

    /**
     * Trouver tous les commentaires d'un deal
     */
    public List<CommentaireDTO> trouverParDeal(UUID dealUuid) {
        log.debug("Recherche des commentaires du deal: {}", dealUuid);

        return commentaireService.lireTous(dealUuid)
                .stream()
                .map(mapper::modeleVersDto)
                .collect(Collectors.toList());
    }

    /**
     * Mettre à jour un commentaire
     */
    public CommentaireDTO mettreAJour(UUID uuid, CommentaireDTO dto) {
        log.debug("Mise à jour du commentaire: {}", uuid);

        // S'assurer que l'UUID est défini dans le DTO
        dto.setUuid(uuid);

        CommentaireModele modele = mapper.dtoVersModele(dto);
        CommentaireModele mis_a_jour = commentaireService.mettreAJour(uuid, modele);

        return mapper.modeleVersDto(mis_a_jour);
    }

    /**
     * Supprimer un commentaire
     */
    public void supprimer(UUID uuid) {
        log.debug("Suppression du commentaire: {}", uuid);
        commentaireService.supprimerParUuid(uuid);
    }
}

