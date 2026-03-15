package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.enumeration.StatutCommande;
import com.ulr.paytogether.core.enumeration.StatutCommandeUtilisateur;
import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.core.modele.CommandeUtilisateurModele;
import com.ulr.paytogether.core.provider.CommandeProvider;
import com.ulr.paytogether.provider.adapter.entity.CommandeJpa;
import com.ulr.paytogether.provider.adapter.entity.CommandeUtilisateurJpa;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.CommandeJpaMapper;
import com.ulr.paytogether.provider.adapter.mapper.CommandeUtilisateurJpaMapper;
import com.ulr.paytogether.provider.repository.CommandeRepository;
import com.ulr.paytogether.provider.repository.CommandeUtilisateurRepository;
import com.ulr.paytogether.provider.repository.DealRepository;
import com.ulr.paytogether.provider.repository.PaiementRepository;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
@Slf4j
public class CommandeProviderAdapter implements CommandeProvider {

    private final CommandeRepository jpaRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DealRepository dealRepository;
    private final PaiementRepository paiementRepository;
    private final CommandeUtilisateurRepository commandeUtilisateurRepository;
    private final CommandeJpaMapper mapper;
    private final CommandeUtilisateurJpaMapper commandeUtilisateurMapper;

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
    public List<CommandeModele> trouverParMarchandAvecInfosCompletes(UUID marchandUuid) {
        log.info("Récupération des commandes du marchand: {}", marchandUuid);
        
        if (marchandUuid == null) {
            throw new IllegalArgumentException("L'UUID du marchand ne peut pas être null");
        }
        
        UtilisateurJpa marchandJpa = utilisateurRepository.findById(marchandUuid)
                .orElseThrow(() -> new IllegalArgumentException("Marchand non trouvé avec l'UUID: " + marchandUuid));
        
        return jpaRepository.findByMarchandJpa(marchandJpa).stream()
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
    
    @Override
    @Transactional
    public CommandeModele mettreAJourStatutEtDatePayout(UUID commandeUuid, StatutCommande statut, LocalDateTime dateDepotPayout) {
        CommandeJpa commande = jpaRepository.findById(commandeUuid)
            .orElseThrow(() -> new RuntimeException("Commande non trouvée avec l'UUID : " + commandeUuid));

        commande.setStatut(statut);
        if (dateDepotPayout != null) {
            commande.setDateDepotPayout(dateDepotPayout);
        }

        List<CommandeUtilisateurJpa> commandeUtilisateurJpas = commande.getPaiements()
            .stream()
                .map(paiement -> CommandeUtilisateurJpa.builder()
                    .commandeJpa(commande)
                    .utilisateurJpa(paiement.getUtilisateurJpa())
                    .statutCommandeUtilisateur(StatutCommandeUtilisateur.EN_ATTENTE)
                    .build())
                .toList();
        
        CommandeJpa sauvegarde = jpaRepository.save(commande);
        commandeUtilisateurRepository.saveAll(commandeUtilisateurJpas);
        return mapper.versModele(sauvegarde);
    }
    
    @Override
    @Transactional
    public CommandeModele mettreAJourFactureMarchand(UUID commandeUuid, String factureUrl) {
        CommandeJpa commande = jpaRepository.findById(commandeUuid)
            .orElseThrow(() -> new RuntimeException("Commande non trouvée avec l'UUID : " + commandeUuid));
        
        commande.setFactureMarchandUrl(factureUrl);
        commande.setStatut(StatutCommande.INVOICE_SELLER);
        
        CommandeJpa sauvegarde = jpaRepository.save(commande);
        return mapper.versModele(sauvegarde);
    }
    
    @Override
    public List<CommandeUtilisateurModele> trouverUtilisateursCommande(UUID commandeUuid) {
        return commandeUtilisateurRepository.findByCommandeJpaUuid(commandeUuid)
            .stream()
            .map(commandeUtilisateurMapper::versModele)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void validerUtilisateurCommande(UUID commandeUuid, UUID utilisateurUuid) {
        CommandeUtilisateurJpa commandeUtilisateur = commandeUtilisateurRepository
            .findByCommandeJpaUuidAndUtilisateurJpaUuid(commandeUuid, utilisateurUuid)
            .orElseThrow(() -> new RuntimeException(
                "Utilisateur " + utilisateurUuid + " non trouvé pour la commande " + commandeUuid));
        
        commandeUtilisateur.setStatutCommandeUtilisateur(StatutCommandeUtilisateur.VALIDEE);
        commandeUtilisateurRepository.save(commandeUtilisateur);
    }
    
    @Override
    public boolean tousUtilisateursValides(UUID commandeUuid) {
        List<CommandeUtilisateurJpa> utilisateurs = commandeUtilisateurRepository.findByCommandeJpaUuid(commandeUuid);
        if (utilisateurs.isEmpty()) {
            return false;
        }
        
        long nombreValides = commandeUtilisateurRepository
            .countByCommandeJpaUuidAndStatutCommandeUtilisateur(commandeUuid, StatutCommandeUtilisateur.VALIDEE);
        
        return nombreValides == utilisateurs.size();
    }

    @Override
    public CommandeModele mettreAJour(UUID commandeUuid, StatutCommande statut) {
        CommandeJpa commande = jpaRepository.findById(commandeUuid)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée avec l'UUID : " + commandeUuid));
        commande.setStatut(statut);
        CommandeJpa sauvegarde = jpaRepository.save(commande);
        return mapper.versModele(sauvegarde);
    }

}
