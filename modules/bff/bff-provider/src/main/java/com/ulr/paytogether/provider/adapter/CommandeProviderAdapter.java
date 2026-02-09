package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.core.provider.CommandeProvider;
import com.ulr.paytogether.provider.adapter.entity.CommandeJpa;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.CommandeJpaMapper;
import com.ulr.paytogether.provider.repository.CommandeRepository;
import com.ulr.paytogether.provider.repository.DealRepository;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
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
    private final UtilisateurRepository utilisateurRepository;
    private final DealRepository dealRepository;
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
        UtilisateurJpa marchandJpa = utilisateurRepository.findById(utilisateurUuid)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'UUID : " + utilisateurUuid));

        return jpaRepository.findByMarchandJpa(marchandJpa)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommandeModele> trouverParDeal(UUID dealUuid) {
        DealJpa dealJpa = dealRepository.findById(dealUuid)
                .orElseThrow(() -> new RuntimeException("Deal non trouvé avec l'UUID : " + dealUuid));

        return jpaRepository.findByDealJpa(dealJpa)
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
