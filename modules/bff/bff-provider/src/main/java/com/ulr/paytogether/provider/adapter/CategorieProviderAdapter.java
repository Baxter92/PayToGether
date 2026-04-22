package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.core.modele.PageModele;
import com.ulr.paytogether.core.provider.CategorieProvider;
import com.ulr.paytogether.provider.adapter.entity.CategorieJpa;
import com.ulr.paytogether.provider.adapter.mapper.CategorieJpaMapper;
import com.ulr.paytogether.provider.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // Invalide toutes les entrees categories + categorie lors de la creation
    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "categorie",  allEntries = true)
    })
    @Override
    public CategorieModele sauvegarder(CategorieModele categorie) {
        CategorieJpa entite = mapper.versEntite(categorie);
        CategorieJpa sauvegarde = jpaRepository.save(entite);
        return mapper.versModele(sauvegarde);
    }

    // Cache le detail par UUID — cle : uuid en string pour eviter pb de serialisation
    @Cacheable(value = "categorie", key = "#uuid.toString()")
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

    // Cache la liste complete — cle fixe 'all'
    @Cacheable(value = "categories", key = "'all'")
    @Override
    public List<CategorieModele> trouverTous() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    // Cache les pages — cle composee page+size
    @Cacheable(value = "categories", key = "'page:' + #page + ':size:' + #size")
    @Override
    public PageModele<CategorieModele> trouverTous(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nom"));
        Page<CategorieJpa> pageJpa = jpaRepository.findAll(pageable);

        List<CategorieModele> content = pageJpa.getContent().stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());

        return PageModele.<CategorieModele>builder()
                .content(content)
                .page(pageJpa.getNumber())
                .size(pageJpa.getSize())
                .totalElements(pageJpa.getTotalElements())
                .totalPages(pageJpa.getTotalPages())
                .first(pageJpa.isFirst())
                .last(pageJpa.isLast())
                .build();
    }

    // Invalide la liste + l'entree specifique lors de la mise a jour
    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "categorie",  key = "#uuid.toString()")
    })
    @Override
    public CategorieModele mettreAJour(UUID uuid, CategorieModele categorie) {
        return jpaRepository.findById(uuid)
                .map(categorieExistante -> {
                    mapper.mettreAJour(categorieExistante, categorie);
                    CategorieJpa sauvegarde = jpaRepository.save(categorieExistante);
                    return mapper.versModele(sauvegarde);
                })
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'UUID: " + uuid));
    }

    // Invalide la liste + l'entree specifique lors de la suppression
    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "categorie",  key = "#uuid.toString()")
    })
    @Override
    public void supprimerParUuid(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }

    @Override
    public boolean existeParNom(String nom) {
        return jpaRepository.existsByNom(nom);
    }
}
