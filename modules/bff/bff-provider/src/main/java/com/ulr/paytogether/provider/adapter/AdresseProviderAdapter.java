package com.ulr.paytogether.provider.adapter;


import com.ulr.paytogether.core.modele.AdresseModele;
import com.ulr.paytogether.core.provider.AdresseProvider;
import com.ulr.paytogether.provider.adapter.entity.AdresseJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.AdresseJpaMapper;
import com.ulr.paytogether.provider.repository.AdresseRepository;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur JPA pour l'adresse
 * Implémente le port AdresseProvider défini dans bff-core
 * Fait le pont entre le domaine métier et la couche de persistence JPA
 */
@Component
@RequiredArgsConstructor
public class AdresseProviderAdapter implements AdresseProvider {
    private final UtilisateurRepository utilisateurRepository;

    private final AdresseRepository jpaRepository;
    private final AdresseJpaMapper mapper;

    @Override
    public AdresseModele sauvegarder(AdresseModele adresse) {
        AdresseJpa entite = mapper.versEntite(adresse);
        AdresseJpa sauvegarde = jpaRepository.save(entite);
        return mapper.versModele(sauvegarde);
    }

    @Override
    public Optional<AdresseModele> trouverParUuid(UUID uuid) {
        return jpaRepository.findById(uuid)
                .map(mapper::versModele);
    }

    @Override
    public List<AdresseModele> trouverParUtilisateur(UUID utilisateurUuid) {
        UtilisateurJpa utilisateurJpa = utilisateurRepository.findById(utilisateurUuid)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'UUID : " + utilisateurUuid));

        return jpaRepository.findByUtilisateurJpa(utilisateurJpa)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdresseModele> trouverTous() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }
}
