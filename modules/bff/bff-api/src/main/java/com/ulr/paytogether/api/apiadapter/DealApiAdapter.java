package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.DealDTO;
import com.ulr.paytogether.api.dto.DealResponseDto;
import com.ulr.paytogether.api.mapper.DealMapper;
import com.ulr.paytogether.core.domaine.service.DealService;
import com.ulr.paytogether.core.enumeration.StatutDeal;
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

    public DealResponseDto mettreAJour(UUID uuid, DealDTO dto) {
        var dealModele = dealMapper.versEntite(dto);
        var updated = dealService.mettreAJour(uuid, dealModele);
        return dealMapper.versDTO(updated);
    }

    public void supprimerParUuid(UUID uuid) {
        dealService.supprimerParUuid(uuid);
    }

    public Set<String> lireVillesDisponibles() {
        return dealService.lireVillesDisponibles();
    }
}
