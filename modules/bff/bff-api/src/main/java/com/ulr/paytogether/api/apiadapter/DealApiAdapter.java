package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.DealDTO;
import com.ulr.paytogether.api.dto.DealResponseDto;
import com.ulr.paytogether.api.dto.MiseAJourDealDTO;
import com.ulr.paytogether.api.dto.MiseAJourImagesDealDTO;
import com.ulr.paytogether.api.mapper.DealMapper;
import com.ulr.paytogether.core.domaine.service.DealService;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.core.enumeration.StatutImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealApiAdapter {
    private final DealService dealService;
    private final DealMapper dealMapper;


    public DealResponseDto creerDeal( DealDTO dto) {
        var dealModele = dealMapper.versEntite(dto);
        var created = dealService.creer(dealModele);

        return dealMapper.versDTO(created);
    }

    public Optional<DealResponseDto> lireParUuid(UUID uuid) {
        var dealOpt = dealService.lireParUuid(uuid);
        return dealOpt.map(dealMapper::versDTO);
    }

    public List<DealResponseDto> lireTous() {
        var deals = dealService.lireTous();
        return deals.stream()
                .map(dealMapper::versDTO)
                .toList();
    }

    public List<DealResponseDto> lireTousByStatut(StatutDeal statut) {
        var deals = dealService.lireParStatut(statut);
        return deals.stream()
                .map(dealMapper::versDTO)
                .toList();
    }

    public List<DealResponseDto> lireTousByCreateurUuid(UUID uuid) {
        var deals = dealService.lireParCreateur(uuid);
        return deals.stream()
                .map(dealMapper::versDTO)
                .toList();
    }

    public List<DealResponseDto> lireTousByCategorieUuid(UUID uuid) {
        var deals = dealService.lireParCategorie(uuid);
        return deals.stream()
                .map(dealMapper::versDTO)
                .toList();
    }

    public DealResponseDto mettreAJour(UUID uuid, MiseAJourDealDTO dto) {
        var dealModele = dealMapper.versEntite(dto);
        dealModele.setUuid(uuid);

        // mettreAJour retourne le deal avec UNIQUEMENT les nouvelles images si listeImages est présente
        var updated = dealService.mettreAJour(uuid, dealModele);

        // Si le DTO contenait des images, la réponse contiendra uniquement les nouvelles images avec presignUrl
        // Sinon, la réponse contiendra toutes les images existantes
        return dealMapper.versDTO(updated);
    }

    public DealResponseDto mettreAJourStatut(UUID uuid, StatutDeal statut) {
        var updated = dealService.mettreAJourStatut(uuid, statut);
        return dealMapper.versDTO(updated);
    }

    public DealResponseDto mettreAJourImages(UUID uuid, MiseAJourImagesDealDTO dto) {
        var dealModele = com.ulr.paytogether.core.modele.DealModele.builder()
                .uuid(uuid)
                .listeImages(dto.getListeImages().stream()
                        .map(dealMapper::imageDtoVersModele)
                        .toList())
                .build();

        var updated = dealService.mettreAJourImages(uuid, dealModele);
        return dealMapper.versDTO(updated);
    }

    public void supprimerParUuid(UUID uuid) {
        dealService.supprimerParUuid(uuid);
    }

    public Set<String> lireVillesDisponibles() {
        return dealService.lireVillesDisponibles();
    }

    public void mettreAJourStatutImage(UUID dealUuid, UUID imageUuid, StatutImage statut) {
        dealService.mettreAJourStatutImage(dealUuid, imageUuid, statut);
    }

    public String obtenirUrlLectureImage(UUID dealUuid, UUID imageUuid) {
        return dealService.obtenirUrlLectureImage(dealUuid, imageUuid);
    }
}
