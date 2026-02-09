package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.PubliciteDTO;
import com.ulr.paytogether.api.mapper.PubliciteMapper;
import com.ulr.paytogether.core.domaine.service.PubliciteService;
import com.ulr.paytogether.core.modele.PubliciteModele;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur API pour les publicités
 * Fait le pont entre la couche API et la couche service
 */
@Component
@RequiredArgsConstructor
public class PubliciteApiAdapter {

    private final PubliciteService publiciteService;
    private final PubliciteMapper mapper;

    public PubliciteDTO creer(PubliciteDTO publiciteDTO) {
        PubliciteModele modele = mapper.dtoVersModele(publiciteDTO);
        PubliciteModele resultat = publiciteService.creer(modele);
        return mapper.modeleVersDto(resultat);
    }

    public Optional<PubliciteDTO> trouverParUuid(UUID uuid) {
        return publiciteService.lireParUuid(uuid)
                .map(mapper::modeleVersDto);
    }

    public List<PubliciteDTO> trouverTous() {
        return publiciteService.lireTous()
                .stream()
                .map(mapper::modeleVersDto)
                .collect(Collectors.toList());
    }

    public List<PubliciteDTO> trouverActives() {
        return publiciteService.lireActives()
                .stream()
                .map(mapper::modeleVersDto)
                .collect(Collectors.toList());
    }

    public PubliciteDTO mettreAJour(UUID uuid, PubliciteDTO publiciteDTO) {
        PubliciteModele modele = mapper.dtoVersModele(publiciteDTO);
        PubliciteModele resultat = publiciteService.mettreAJour(uuid, modele);
        return mapper.modeleVersDto(resultat);
    }

    public void supprimerParUuid(UUID uuid) {
        publiciteService.supprimerParUuid(uuid);
    }
}
