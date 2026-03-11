package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.enumeration.StatutCommande;
import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.core.provider.CommandeProvider;
import com.ulr.paytogether.provider.adapter.entity.CommandeJpa;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.CommandeJpaMapper;
import com.ulr.paytogether.provider.repository.CommandeRepository;
import com.ulr.paytogether.provider.repository.DealRepository;
import com.ulr.paytogether.provider.repository.PaiementRepository;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final PaiementRepository paiementRepository;
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
    public CommandeModele trouverParDeal(UUID dealUuid) {
        DealJpa dealJpa = dealRepository.findById(dealUuid)
                .orElseThrow(() -> new RuntimeException("Deal non trouvé avec l'UUID : " + dealUuid));

        return jpaRepository.findByDealJpa(dealJpa)
                .map(mapper::versModele)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée pour le deal avec l'UUID : " + dealUuid));
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

    @Override
    public List<CommandeModele> trouverToutesAvecInfosCompletes() {
        return jpaRepository.findAll().stream()
                .map(commandeJpa -> {
                    CommandeModele commande = mapper.versModele(commandeJpa);

                    // Calculer le montant total des paiements pour cette commande
                    var montantTotal = paiementRepository.findByCommandeJpaUuid(commandeJpa.getUuid())
                            .stream()
                            .map(p -> p.getMontant())
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

                    commande.setMontantTotalPaiements(montantTotal);

                    return commande;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> calculerStatistiquesCommandes() {
        List<CommandeJpa> toutesLesCommandes = jpaRepository.findAll();

        long totalCommandes = toutesLesCommandes.size();
        long commandesConfirmees = toutesLesCommandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.CONFIRMEE)
                .count();
        long commandesEnCours = toutesLesCommandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.EN_COURS)
                .count();
        long commandesAnnulees = toutesLesCommandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.ANNULEE)
                .count();
        long commandesRemboursees = toutesLesCommandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.REMBOURSEE)
                .count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalCommandes", totalCommandes);
        stats.put("commandesConfirmees", commandesConfirmees);
        stats.put("commandesEnCours", commandesEnCours);
        stats.put("commandesAnnulees", commandesAnnulees);
        stats.put("commandesRemboursees", commandesRemboursees);

        return stats;
    }

    @Override
    public CommandeModele trouverParPaiementUuid(UUID paiementUuid) {
        return jpaRepository.findByPaiementUuid(paiementUuid)
                .map(mapper::versModele)
                .orElseThrow(()-> new RuntimeException("Aucune commande trouvée pour le paiement UUID : " + paiementUuid));
    }

    @Override
    public CommandeModele lireParUuid(UUID uuid) {
        return jpaRepository.findById(uuid)
                .map(mapper::versModele)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée avec l'UUID : " + uuid));
    }
}
