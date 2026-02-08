package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.core.provider.CategorieProvider;
import com.ulr.paytogether.provider.adapter.entity.CategorieJpa;
import com.ulr.paytogether.provider.adapter.mapper.CategorieJpaMapper;
import com.ulr.paytogether.provider.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur JPA pour la catégorie
 * Implémente le port CategorieProvider défini dans bff-core
 * Fait le pont entre le domaine métier et la couche de persistence JPA
 */
@Component
@RequiredArgsConstructor
public class CategorieProviderAdapter implements CategorieProvider {

    private final CategorieRepository jpaRepository;
    private final CategorieJpaMapper mapper;

    @Override
    public CategorieModele sauvegarder(CategorieModele categorie) {
        CategorieJpa entite = mapper.versEntite(categorie);
        CategorieJpa sauvegarde = jpaRepository.save(entite);
        return mapper.versModele(sauvegarde);
    }

    @Override
    public Optional<CategorieModele> trouverParUuid(UUID uuid) {
        return jpaRepository.findById(uuid)
                .map(mapper::versModele);
    }

    @Override
    public Optional<CategorieModele> trouverParNom(String nom) {
        return jpaRepository.findByNom(nom)
                .map(mapper::versModele);
    }

    @Override
    public List<CategorieModele> trouverTous() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }

    @Override
    public boolean existeParNom(String nom) {
        return jpaRepository.existsByNom(nom);
    }
}
