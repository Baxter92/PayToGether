package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.enumeration.StatutCommande;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.core.provider.DealProvider;
import com.ulr.paytogether.core.provider.PaiementProvider;
import com.ulr.paytogether.provider.adapter.entity.*;
import com.ulr.paytogether.provider.adapter.mapper.AdresseJpaMapper;
import com.ulr.paytogether.provider.adapter.mapper.CommandeJpaMapper;
import com.ulr.paytogether.provider.adapter.mapper.DealJpaMapper;
import com.ulr.paytogether.provider.adapter.mapper.PaiementJpaMapper;
import com.ulr.paytogether.provider.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.ulr.paytogether.provider.utils.Tools.extractUserIdFromToken;

/**
 * Adaptateur JPA pour le paiement
 * Implémente le port PaiementProvider défini dans bff-core
 * Fait le pont entre le domaine métier et la couche de persistence JPA
 * Support Square Payment avec méthodes additionnelles
 */
@Component
@RequiredArgsConstructor
public class PaiementProviderAdapter implements PaiementProvider {

    private final PaiementRepository jpaRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PaiementJpaMapper mapper;
    private final DealJpaMapper dealJpaMapper;
    private final CommandeJpaMapper commandeJpaMapper;
    private final CommandeRepository commandeRepository;
    private final DealRepository dealRepository;
    private final DealProvider dealProvider;
    private final DealParticipantRepository dealParticipantRepository;
    private final AdresseRepository adresseRepository;
    private final AdresseJpaMapper adresseJpaMapper;
    private final CommandeUtilisateurRepository  commandeUtilisateurRepository;

    @Override
    public PaiementModele sauvegarder(PaiementModele paiement) {
        PaiementJpa entite = mapper.versEntite(paiement);
        DealJpa dealJpa = dealProvider.trouverParUuid(paiement.getDeal().getUuid())
                .map(dealJpaMapper::versEntite)
                .orElseThrow(() -> new RuntimeException("Deal non trouvé pour l'UUID : " + paiement.getDeal().getUuid()));

        Optional<UUID> utilisateurJpaOptional = dealJpa.getParticipants().stream()
                .map(participant -> participant.getUtilisateurJpa().getUuid())
                .filter(uuid -> uuid.equals(paiement.getUtilisateur().getUuid()))
                .findFirst();
        if (utilisateurJpaOptional.isPresent()) {
            throw new IllegalArgumentException(("Utilisateur avec UUID " + paiement.getUtilisateur().getUuid() + " est déjà participant du deal " + dealJpa.getUuid()));
        }

        int nbParticipants = dealParticipantRepository.findByIdDealUuid(dealJpa.getUuid()).stream().map(DealParticipantJpa::getNombreDePart).reduce(0, Integer::sum);
        if (nbParticipants >= dealJpa.getNbParticipants()) {
            throw new IllegalStateException("Le deal " + dealJpa.getUuid() + " a déjà atteint le nombre maximum de participants (" + dealJpa.getNbParticipants() + ").");
        }

        Optional<CommandeJpa> commandeJpaOptional = commandeRepository.findByDealJpa(dealJpa);
        CommandeJpa commandeJpa;
        if (commandeJpaOptional.isPresent()) {
            commandeJpa = commandeJpaOptional.get();
        } else {
            // Créer et sauvegarder la nouvelle commande AVANT de l'assigner au paiement
            commandeJpa = commandeJpaMapper.fromDealJpa(dealJpa);
            commandeJpa = commandeRepository.save(commandeJpa);
        }
        entite.setCommandeJpa(commandeJpa);
        PaiementJpa sauvegarde = jpaRepository.save(entite);

        AdresseJpa adresseJpa = adresseJpaMapper.versEntite(paiement.getAdresse());
        adresseJpa.setPaiement(sauvegarde);
        adresseRepository.save(adresseJpa);

        PaiementModele paiementCree = mapper.versModele(sauvegarde);
        paiementCree.setNombreDePart(paiement.getNombreDePart());

        return paiementCree;
    }

    @Override
    public PaiementModele mettreAJour(UUID uuid, PaiementModele paiement) {
        PaiementJpa entite = jpaRepository.findById(uuid)
            .orElseThrow(() -> new IllegalArgumentException("Paiement non trouvé : " + uuid));

        // Mettre à jour les champs
        entite.setMontant(paiement.getMontant());
        entite.setStatut(paiement.getStatut());
        entite.setMethodePaiement(paiement.getMethodePaiement());
        entite.setTransactionId(paiement.getTransactionId());
        entite.setSquarePaymentId(paiement.getSquarePaymentId());
        entite.setSquareOrderId(paiement.getSquareOrderId());
        entite.setSquareLocationId(paiement.getSquareLocationId());
        entite.setSquareReceiptUrl(paiement.getSquareReceiptUrl());
        entite.setSquareToken(paiement.getSquareToken());
        entite.setMessageErreur(paiement.getMessageErreur());
        entite.setDatePaiement(paiement.getDatePaiement());

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
        return jpaRepository.findByTransactionId(transactionId)
                .map(mapper::versModele);
    }

    @Override
    public Optional<PaiementModele> trouverParSquarePaymentId(String squarePaymentId) {
        return jpaRepository.findBySquarePaymentId(squarePaymentId)
                .map(mapper::versModele);
    }

    @Override
    public List<PaiementModele> trouverParUtilisateur(UUID utilisateurUuid) {
        UtilisateurJpa utilisateurJpa = utilisateurRepository.findById(utilisateurUuid)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé pour l'UUID : " + utilisateurUuid));

        return jpaRepository.findByUtilisateurJpaOrderByDatePaiementDesc(utilisateurJpa)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaiementModele> trouverParCommande(UUID commandeUuid) {
        return jpaRepository.findByCommandeJpaUuidOrderByDatePaiementDesc(commandeUuid)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
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
        adresseRepository.findByPaiementUuid(uuid).ifPresent(adresseRepository::delete);
        jpaRepository.deleteById(uuid);
    }

    @Override
    public PaiementModele mettreAJourStatutCommandeDeal(UUID paiementUuid, String statut, int nombreDePart) {
        PaiementJpa paiementJpa = jpaRepository.findById(paiementUuid)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé pour l'UUID : " + paiementUuid));

        CommandeJpa commandeJpa = paiementJpa.getCommandeJpa();
        if (commandeJpa != null && StatutPaiement.CONFIRME.name().equals(statut)) {
            DealJpa dealJpa = commandeJpa.getDealJpa();

            // Vérifier si l'utilisateur n'est pas déjà participant
            boolean dejaParticipant = dealJpa.getParticipants().stream()
                    .anyMatch(p -> p.getUtilisateurJpa().getUuid().equals(paiementJpa.getUtilisateurJpa().getUuid()));

            if (!dejaParticipant) {
                // Créer une nouvelle participation
                DealParticipantJpa participation = DealParticipantJpa.builder()
                        .id(new DealParticipantJpa.DealParticipantId(dealJpa.getUuid(), paiementJpa.getUtilisateurJpa().getUuid()))
                        .dealJpa(dealJpa)
                        .utilisateurJpa(paiementJpa.getUtilisateurJpa())
                        .nombreDePart(nombreDePart) // Par défaut 1 part si non spécifié
                        .build();
                dealJpa.getParticipants().add(participation);
            }

            var dealComplete = dealJpa.getNbParticipants() == dealJpa.getParticipants().size();
            StatutCommande statutCommande = dealComplete ? StatutCommande.COMPLETEE : StatutCommande.EN_COURS;

            dealRepository.save(dealJpa);
            commandeJpa.setStatut(statutCommande);
            commandeRepository.save(commandeJpa);
        }
        return mapper.versModele(paiementJpa);
    }

    @Override
    public List<PaiementModele> trouverTousAvecInfosCompletes() {
        return jpaRepository.findAll().stream()
                .map(mapper::versModeleComplet)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> calculerStatistiquesPaiements() {
        List<PaiementJpa> tousLesPaiements = jpaRepository.findAll();

        long totalTransactions = tousLesPaiements.size();
        long transactionsReussies = tousLesPaiements.stream()
                .filter(p -> p.getStatut() == StatutPaiement.CONFIRME)
                .count();
        long transactionsEchouees = tousLesPaiements.stream()
                .filter(p -> p.getStatut() == StatutPaiement.ECHOUE)
                .count();
        BigDecimal montantTotal = tousLesPaiements.stream()
                .filter(p -> p.getStatut() == StatutPaiement.CONFIRME)
                .map(PaiementJpa::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTransactions", totalTransactions);
        stats.put("transactionsReussies", transactionsReussies);
        stats.put("transactionsEchouees", transactionsEchouees);
        stats.put("montantTotal", montantTotal);

        return stats;
    }

    @Override
    public List<PaiementModele> trouverParUtilisateurAvecInfosCompletes(String keycloakId) {
        String userId = extractUserIdFromToken(keycloakId);
        UtilisateurJpa utilisateurJpa = utilisateurRepository.findByKeycloakId(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé pour le Keycloak ID : " + userId));

        return jpaRepository.findByUtilisateurJpaOrderByDatePaiementDesc(utilisateurJpa)
                .stream()
                .map(mapper::versModeleComplet)
                .collect(Collectors.toList());
    }

    @Override
    public void supprimerParUtilisateur(UUID uuid) {
        UtilisateurJpa utilisateurJpa = utilisateurRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé pour l'UUID : " + uuid));

        List<PaiementJpa> paiements = jpaRepository.findByUtilisateurJpaOrderByDatePaiementDesc(utilisateurJpa);
        List<CommandeJpa> commandeJpas = new ArrayList<>();
        List<CommandeUtilisateurJpa> commandeUtilisateurJpas = new ArrayList<>();
        paiements.forEach(p -> {;
            commandeJpas.add(p.getCommandeJpa());
            commandeUtilisateurJpas.addAll(commandeUtilisateurRepository.findByCommandeJpaUuid(p.getCommandeJpa().getUuid()));
        });
        adresseRepository.deleteAllByPaiement(paiements);
        jpaRepository.deleteAll(paiements);
        if (!commandeUtilisateurJpas.isEmpty()) {
            commandeUtilisateurRepository.deleteAll(commandeUtilisateurJpas);
        }
        if (!commandeJpas.isEmpty()) {
            commandeJpas.forEach(commande -> {
                CommandeJpa cJpa = commandeRepository.getReferenceById(commande.getUuid());
                if (cJpa.getPaiements().isEmpty()) {
                    commandeRepository.delete(cJpa);
                }
            });
        }
    }

    @Override
    public void dettacherCommande(UUID uuid) {
        PaiementJpa paiementJpa = jpaRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé pour l'UUID : " + uuid));

        CommandeJpa commandeJpa = paiementJpa.getCommandeJpa();
        if (commandeJpa != null) {
            paiementJpa.setCommandeJpa(null);
            jpaRepository.save(paiementJpa);

            // Vérifier si la commande n'a plus de paiements associés
            if (commandeJpa.getPaiements().isEmpty()) {
                commandeRepository.delete(commandeJpa);
            }
        }
    }
}
