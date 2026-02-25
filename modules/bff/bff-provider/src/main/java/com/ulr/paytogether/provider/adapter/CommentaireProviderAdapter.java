package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.exception.ResourceNotFoundException;
import com.ulr.paytogether.core.modele.CommentaireModele;
import com.ulr.paytogether.core.provider.CommentaireProvider;
import com.ulr.paytogether.provider.adapter.entity.CommentaireJpa;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.CommentaireJpaMapper;
import com.ulr.paytogether.provider.repository.CommentaireRepository;
import com.ulr.paytogether.provider.repository.DealRepository;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur JPA pour le commentaire
 * Implémente le port CommentaireProvider défini dans bff-core
 * Fait le pont entre le domaine métier et la couche de persistence JPA
 */
@Component
@RequiredArgsConstructor
public class CommentaireProviderAdapter implements CommentaireProvider {

    private final CommentaireRepository jpaRepository;
    private final DealRepository dealRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CommentaireJpaMapper mapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommentaireModele sauvegarder(CommentaireModele commentaire) {
        CommentaireJpa entite = mapper.versEntite(commentaire);
        CommentaireJpa sauvegarde = jpaRepository.save(entite);
        return mapper.versModele(sauvegarde);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommentaireModele mettreAJour(UUID uuid, CommentaireModele commentaire) {
        return jpaRepository.findById(uuid)
                .map(jpa -> {
                    mapper.mettreAJour(jpa, commentaire);
                    CommentaireJpa sauvegarde = jpaRepository.save(jpa);
                    return mapper.versModele(sauvegarde);
                })
                .orElseThrow(() -> ResourceNotFoundException.parUuid("commentaire", uuid));
    }

    @Override
    public Optional<CommentaireModele> trouverParUuid(UUID uuid) {
        return jpaRepository.findById(uuid)
                .map(mapper::versModele);
    }

    @Override
    public List<CommentaireModele> trouverParDeal(UUID dealUuid) {
        DealJpa dealJpa = dealRepository.findById(dealUuid)
                .orElseThrow(() -> ResourceNotFoundException.parUuid("deal", dealUuid));

        return jpaRepository.findByDealJpa(dealJpa)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentaireModele> trouverParUtilisateur(UUID utilisateurUuid) {
        UtilisateurJpa utilisateurJpa = utilisateurRepository.findById(utilisateurUuid)
                .orElseThrow(() -> ResourceNotFoundException.parUuid("utilisateur", utilisateurUuid));

        return jpaRepository.findByUtilisateurJpa(utilisateurJpa)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentaireModele> trouverTous() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void supprimerParUuid(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }
}
