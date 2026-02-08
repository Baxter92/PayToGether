package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.core.provider.CommandeProvider;
import com.ulr.paytogether.provider.adapter.entity.CommandeJpa;
import com.ulr.paytogether.provider.adapter.mapper.CommandeJpaMapper;
import com.ulr.paytogether.provider.repository.CommandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur JPA pour la commande
 * Implémente le port CommandeProvider défini dans bff-core
 * Fait le pont entre le domaine métier et la couche de persistence JPA
 */
@Component
@RequiredArgsConstructor
public class CommandeProviderAdapter implements CommandeProvider {

    private final CommandeRepository jpaRepository;
    private final CommandeJpaMapper mapper;

    @Override
    public CommandeModele sauvegarder(CommandeModele commande) {
        CommandeJpa entite = mapper.versEntite(commande);
        CommandeJpa sauvegarde = jpaRepository.save(entite);
        return mapper.versModele(sauvegarde);
    }

    @Override
    public Optional<CommandeModele> trouverParUuid(UUID uuid) {
        return jpaRepository.findById(uuid)
                .map(mapper::versModele);
    }

    @Override
    public List<CommandeModele> trouverParUtilisateur(UUID utilisateurUuid) {
        return jpaRepository.findByUtilisateurUuid(utilisateurUuid)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommandeModele> trouverParDeal(UUID dealUuid) {
        return jpaRepository.findByDealUuid(dealUuid)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommandeModele> trouverTous() {
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
