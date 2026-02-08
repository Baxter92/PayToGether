package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.modele.PubliciteModele;
import com.ulr.paytogether.core.provider.PubliciteProvider;
import com.ulr.paytogether.provider.adapter.entity.PubliciteJpa;
import com.ulr.paytogether.provider.adapter.mapper.PubliciteJpaMapper;
import com.ulr.paytogether.provider.repository.PubliciteRepository;
import com.ulr.paytogether.provider.utils.FileManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur JPA pour la publicité
 * Implémente le port PubliciteProvider défini dans bff-core
 * Fait le pont entre le domaine métier et la couche de persistence JPA
 */
@Component
@RequiredArgsConstructor
public class PubliciteProviderAdapter implements PubliciteProvider {

    private final PubliciteRepository jpaRepository;
    private final PubliciteJpaMapper mapper;
    private final FileManager fileManager;

    @Override
    public PubliciteModele sauvegarder(PubliciteModele publicite) {
        PubliciteJpa entite = mapper.versEntite(publicite);
        PubliciteJpa sauvegarde = jpaRepository.save(entite);
        return mapper.versModele(sauvegarde);
    }

    @Override
    public Optional<PubliciteModele> trouverParUuid(UUID uuid) {
        return jpaRepository.findById(uuid)
                .map(mapper::versModele);
    }

    @Override
    public List<PubliciteModele> trouverTous() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public List<PubliciteModele> trouverActives() {
        return jpaRepository.findByActiveTrue()
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public PubliciteModele mettreAJour(UUID uuid, PubliciteModele publicite) {
        return jpaRepository.findById(uuid)
                .map(publiciteExistante -> {
                    mapper.mettreAJour(publiciteExistante, publicite);
                    PubliciteJpa sauvegarde = jpaRepository.save(publiciteExistante);
                    return mapper.versModele(sauvegarde);
                })
                .orElseThrow(() -> new RuntimeException("Publicité non trouvée avec l'UUID: " + uuid));
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }
}
