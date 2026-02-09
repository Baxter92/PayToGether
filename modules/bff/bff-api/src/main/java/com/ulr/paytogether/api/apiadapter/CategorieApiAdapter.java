package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.CategorieDTO;
import com.ulr.paytogether.api.mapper.CategorieMapper;
import com.ulr.paytogether.core.domaine.service.CategorieService;
import com.ulr.paytogether.core.modele.CategorieModele;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur API pour les catégories
 * Fait le pont entre la couche API et la couche service
 */
@Component
@RequiredArgsConstructor
public class CategorieApiAdapter {

    private final CategorieService categorieService;
    private final CategorieMapper mapper;

    public CategorieDTO creer(CategorieDTO categorieDTO) {
        CategorieModele modele = mapper.dtoVersModele(categorieDTO);
        CategorieModele resultat = categorieService.creer(modele);
        return mapper.modeleVersDto(resultat);
    }

    public Optional<CategorieDTO> trouverParUuid(UUID uuid) {
        return categorieService.lireParUuid(uuid)
                .map(mapper::modeleVersDto);
    }

    public Optional<CategorieDTO> trouverParNom(String nom) {
        return categorieService.lireParNom(nom)
                .map(mapper::modeleVersDto);
    }

    public List<CategorieDTO> trouverTous() {
        return categorieService.lireTous()
                .stream()
                .map(mapper::modeleVersDto)
                .collect(Collectors.toList());
    }

    public CategorieDTO mettreAJour(UUID uuid, CategorieDTO categorieDTO) {
        CategorieModele modele = mapper.dtoVersModele(categorieDTO);
        CategorieModele resultat = categorieService.mettreAJour(uuid, modele);
        return mapper.modeleVersDto(resultat);
    }

    public void supprimerParUuid(UUID uuid) {
        categorieService.supprimerParUuid(uuid);
    }

    public boolean existeParNom(String nom) {
        return categorieService.existeParNom(nom);
    }
}
