package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.core.provider.PaiementProvider;
import com.ulr.paytogether.provider.adapter.entity.PaiementJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.PaiementJpaMapper;
import com.ulr.paytogether.provider.repository.PaiementRepository;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur JPA pour le paiement
 * Implémente le port PaiementProvider défini dans bff-core
 * Fait le pont entre le domaine métier et la couche de persistence JPA
 */
@Component
@RequiredArgsConstructor
public class PaiementProviderAdapter implements PaiementProvider {

    private final PaiementRepository jpaRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PaiementJpaMapper mapper;

    @Override
    public PaiementModele sauvegarder(PaiementModele paiement) {
        PaiementJpa entite = mapper.versEntite(paiement);
        PaiementJpa sauvegarde = jpaRepository.save(entite);
        return mapper.versModele(sauvegarde);
    }

    @Override
    public Optional<PaiementModele> trouverParUuid(UUID uuid) {
        return jpaRepository.findById(uuid)
                .map(mapper::versModele);
    }

    @Override
    public Optional<PaiementModele> trouverParTransactionId(String transactionId) {
        // Cette méthode n'est pas implémentée dans le repository actuel
        // On retourne un Optional vide pour le moment
        return Optional.empty();
    }

    @Override
    public List<PaiementModele> trouverParUtilisateur(UUID utilisateurUuid) {
        UtilisateurJpa utilisateurJpa = utilisateurRepository.findById(utilisateurUuid)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé pour l'UUID : " + utilisateurUuid));

        return jpaRepository.findByUtilisateurJpa(utilisateurJpa)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaiementModele> trouverParCommande(UUID commandeUuid) {
        return List.of();
    }

    @Override
    public List<PaiementModele> trouverTous() {
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
