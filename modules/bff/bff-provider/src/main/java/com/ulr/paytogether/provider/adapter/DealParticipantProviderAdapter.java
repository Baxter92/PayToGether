package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.modele.DealParticipantModele;
import com.ulr.paytogether.core.provider.DealParticipantProvider;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.provider.adapter.entity.DealParticipantJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.DealParticipantJpaMapper;
import com.ulr.paytogether.provider.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation du Provider pour gérer les participations aux deals
 * Adaptateur technique (partie droite de l'architecture hexagonale)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DealParticipantProviderAdapter implements DealParticipantProvider {

    private final DealParticipantRepository participantRepository;
    private final DealRepository dealRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DealParticipantJpaMapper mapper;
    private final PaiementRepository paiementRepository;
    private final AdresseRepository adresseRepository;

    @Override
    @Transactional
    public DealParticipantModele ajouterParticipant(DealParticipantModele participation) {
        log.debug("Ajout d'un participant au deal {} : utilisateur {}, {} parts",
                participation.getDealUuid(), participation.getUtilisateurUuid(), participation.getNombreDePart());

        // Vérifier que le deal existe
        DealJpa deal = dealRepository.findById(participation.getDealUuid())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Le deal avec l'UUID " + participation.getDealUuid() + " n'existe pas"));

        // Vérifier que l'utilisateur existe
        UtilisateurJpa utilisateur = utilisateurRepository.findById(participation.getUtilisateurUuid())
                .orElseThrow(() -> new IllegalArgumentException(
                        "L'utilisateur avec l'UUID " + participation.getUtilisateurUuid() + " n'existe pas"));

        // Vérifier si l'utilisateur ne participe pas déjà
        if (participantRepository.existsByIdDealUuidAndIdUtilisateurUuid(
                participation.getDealUuid(), participation.getUtilisateurUuid())) {
            throw new IllegalStateException(
                    "L'utilisateur participe déjà à ce deal. Utilisez mettreAJourNombreDePart() pour modifier le nombre de parts.");
        }

        // Créer l'entité
        DealParticipantJpa jpa = mapper.versEntite(participation);
        jpa.setDealJpa(deal);
        jpa.setUtilisateurJpa(utilisateur);

        // Sauvegarder
        DealParticipantJpa sauvegarde = participantRepository.save(jpa);
        log.info("Participant ajouté avec succès : deal={}, utilisateur={}, parts={}",
                participation.getDealUuid(), participation.getUtilisateurUuid(), participation.getNombreDePart());

        return mapper.versModele(sauvegarde);
    }

    @Override
    @Transactional
    public DealParticipantModele mettreAJourNombreDePart(UUID dealUuid, UUID utilisateurUuid, Integer nombreDePart) {
        log.debug("Mise à jour du nombre de parts : deal={}, utilisateur={}, nouvelles parts={}",
                dealUuid, utilisateurUuid, nombreDePart);

        if (nombreDePart == null || nombreDePart <= 0) {
            throw new IllegalArgumentException("Le nombre de parts doit être supérieur à 0");
        }

        DealParticipantJpa participation = participantRepository
                .findByIdDealUuidAndIdUtilisateurUuid(dealUuid, utilisateurUuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Participation non trouvée pour le deal " + dealUuid + " et l'utilisateur " + utilisateurUuid));

        participation.setNombreDePart(nombreDePart);
        DealParticipantJpa sauvegarde = participantRepository.save(participation);

        log.info("Nombre de parts mis à jour avec succès : deal={}, utilisateur={}, parts={}",
                dealUuid, utilisateurUuid, nombreDePart);

        return mapper.versModele(sauvegarde);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DealParticipantModele> trouverParticipation(UUID dealUuid, UUID utilisateurUuid) {
        log.debug("Recherche de la participation : deal={}, utilisateur={}", dealUuid, utilisateurUuid);
        return participantRepository.findByIdDealUuidAndIdUtilisateurUuid(dealUuid, utilisateurUuid)
                .map(mapper::versModele);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DealParticipantModele> trouverParticipantsParDeal(UUID dealUuid) {
        log.debug("Recherche de tous les participants du deal {}", dealUuid);
        return participantRepository.findByIdDealUuid(dealUuid).stream()
                .map(mapper::versModele)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DealParticipantModele> trouverParticipantsParDealAvecUtilisateur(UUID dealUuid) {
        log.debug("Recherche de tous les participants du deal {} avec infos utilisateur, paiement et adresse", dealUuid);

        // Récupérer le deal pour obtenir le prix par part
        var deal = dealRepository.findById(dealUuid).orElse(null);
        var prixPart = deal != null ? deal.getPrixPart() : null;

        return participantRepository.findByIdDealUuid(dealUuid).stream()
                .map(participantJpa -> {
                    // Récupérer le paiement associé
                    var paiementOpt = paiementRepository.findFirstByUtilisateurJpaUuidAndCommandeJpaDealJpaUuidOrderByDatePaiementDesc(
                            participantJpa.getUtilisateurJpa().getUuid(),
                            dealUuid
                    );

                    // Récupérer l'adresse si le paiement existe
                    var adresseOpt = paiementOpt.flatMap(paiement ->
                            adresseRepository.findByPaiementUuid(paiement.getUuid())
                    );

                    return mapper.versModeleComplet(participantJpa, paiementOpt.orElse(null), adresseOpt.orElse(null), prixPart);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DealParticipantModele> trouverParticipationsParUtilisateur(UUID utilisateurUuid) {
        log.debug("Recherche de toutes les participations de l'utilisateur {}", utilisateurUuid);
        return participantRepository.findByIdUtilisateurUuid(utilisateurUuid).stream()
                .map(mapper::versModele)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean utilisateurParticipeAuDeal(UUID dealUuid, UUID utilisateurUuid) {
        log.debug("Vérification si l'utilisateur {} participe au deal {}", utilisateurUuid, dealUuid);
        return participantRepository.existsByIdDealUuidAndIdUtilisateurUuid(dealUuid, utilisateurUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public long compterParticipants(UUID dealUuid) {
        log.debug("Comptage des participants du deal {}", dealUuid);
        return participantRepository.countByIdDealUuid(dealUuid);
    }

    @Override
    public long compterNombreParts(UUID dealUuid) {
        return participantRepository.findByIdDealUuid(dealUuid)
                .stream()
                .mapToInt(DealParticipantJpa::getNombreDePart)
                .sum();
    }

    @Override
    @Transactional
    public void supprimerParticipant(UUID dealUuid, UUID utilisateurUuid) {
        log.debug("Suppression du participant : deal={}, utilisateur={}", dealUuid, utilisateurUuid);

        DealParticipantJpa.DealParticipantId id = new DealParticipantJpa.DealParticipantId(dealUuid, utilisateurUuid);
        if (!participantRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Participation non trouvée pour le deal " + dealUuid + " et l'utilisateur " + utilisateurUuid);
        }

        participantRepository.deleteById(id);
        log.info("Participant supprimé avec succès : deal={}, utilisateur={}", dealUuid, utilisateurUuid);
    }

    @Override
    @Transactional
    public void supprimerTousLesParticipants(UUID dealUuid) {
        log.debug("Suppression de tous les participants du deal {}", dealUuid);
        participantRepository.deleteByIdDealUuid(dealUuid);
        log.info("Tous les participants du deal {} ont été supprimés", dealUuid);
    }
}

