package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.exception.ResourceNotFoundException;
import com.ulr.paytogether.core.modele.CommentaireModele;
import com.ulr.paytogether.core.modele.PageModele;
import com.ulr.paytogether.core.provider.CommentaireProvider;
import com.ulr.paytogether.provider.adapter.entity.CommentaireJpa;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.CommentaireJpaMapper;
import com.ulr.paytogether.provider.repository.CommentaireRepository;
import com.ulr.paytogether.provider.repository.DealRepository;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        // Gérer le commentaire parent si présent
        if (commentaire.getCommentaireParentUuid() != null) {
            CommentaireJpa parent = jpaRepository.findById(commentaire.getCommentaireParentUuid())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Commentaire parent non trouvé : " + commentaire.getCommentaireParentUuid()));
            entite.setCommentaireParentJpa(parent);
        }

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
    public PageModele<CommentaireModele> trouverParDeal(UUID dealUuid, int page, int size) {
        DealJpa dealJpa = dealRepository.findById(dealUuid)
                .orElseThrow(() -> ResourceNotFoundException.parUuid("deal", dealUuid));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateCreation"));
        Page<CommentaireJpa> pageJpa = jpaRepository.findByDealJpa(dealJpa, pageable);

        List<CommentaireModele> content = pageJpa.getContent().stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());

        return PageModele.<CommentaireModele>builder()
                .content(content)
                .page(pageJpa.getNumber())
                .size(pageJpa.getSize())
                .totalElements(pageJpa.getTotalElements())
                .totalPages(pageJpa.getTotalPages())
                .first(pageJpa.isFirst())
                .last(pageJpa.isLast())
                .build();
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

    @Override
    public PageModele<CommentaireModele> trouverTous(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateCreation"));
        Page<CommentaireJpa> pageJpa = jpaRepository.findAll(pageable);

        List<CommentaireModele> content = pageJpa.getContent().stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());

        return PageModele.<CommentaireModele>builder()
                .content(content)
                .page(pageJpa.getNumber())
                .size(pageJpa.getSize())
                .totalElements(pageJpa.getTotalElements())
                .totalPages(pageJpa.getTotalPages())
                .first(pageJpa.isFirst())
                .last(pageJpa.isLast())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<CommentaireModele> trouverReponsesParCommentaireParent(UUID commentaireParentUuid) {
        CommentaireJpa parent = jpaRepository.findById(commentaireParentUuid)
                .orElseThrow(() -> ResourceNotFoundException.parUuid("commentaire", commentaireParentUuid));

        return jpaRepository.findByCommentaireParentJpa(parent)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mettreAJourFlagPertinent(UUID uuid, Boolean estPertinent) {
        CommentaireJpa commentaire = jpaRepository.findById(uuid)
                .orElseThrow(() -> ResourceNotFoundException.parUuid("commentaire", uuid));

        // Vérifier que c'est bien une réponse
        if (commentaire.getCommentaireParentJpa() == null) {
            throw new IllegalArgumentException("Le flag pertinent ne peut être modifié que sur une réponse (commentaire avec parent)");
        }

        commentaire.setEstPertinent(estPertinent);
        jpaRepository.save(commentaire);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void supprimerParUuid(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }
}
