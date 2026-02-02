package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.core.provider.UtilisateurProvider;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.UtilisateurJpaMapper;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur JPA pour l'utilisateur
 * Implémente le port UtilisateurPort défini dans bff-core
 * Fait le pont entre le domaine métier et la couche de persistence JPA
 */
@Component
@RequiredArgsConstructor
public class UtilisateurProviderAdapter implements UtilisateurProvider {

    private final UtilisateurRepository jpaRepository;
    private final UtilisateurJpaMapper mapper;

    @Override
    public UtilisateurModele sauvegarder(UtilisateurModele utilisateur) {
        UtilisateurJpa entite = mapper.versEntite(utilisateur);
        UtilisateurJpa sauvegarde = jpaRepository.save(entite);
        return mapper.versModele(sauvegarde);
    }

    @Override
    public Optional<UtilisateurModele> trouverParUuid(UUID uuid) {
        return jpaRepository.findById(uuid)
                .map(mapper::versModele);
    }

    @Override
    public Optional<UtilisateurModele> trouverParEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(mapper::versModele);
    }

    @Override
    public List<UtilisateurModele> trouverTous() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public UtilisateurModele mettreAJour(UUID uuid, UtilisateurModele utilisateur) {
        return jpaRepository.findById(uuid)
                .map(utilisateurExistant -> {
                    // Mettre à jour les champs modifiables
                    mapper.mettreAJour(utilisateurExistant, utilisateur);
                    // Sauvegarder et retourner le modèle mis à jour
                    UtilisateurJpa sauvegarde = jpaRepository.save(utilisateurExistant);
                    return mapper.versModele(sauvegarde);
                })
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'UUID: " + uuid));
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }

    @Override
    public boolean existeParEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
