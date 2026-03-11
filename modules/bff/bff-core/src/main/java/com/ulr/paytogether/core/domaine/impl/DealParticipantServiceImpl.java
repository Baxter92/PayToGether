package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.DealParticipantService;
import com.ulr.paytogether.core.modele.DealParticipantModele;
import com.ulr.paytogether.core.provider.DealParticipantProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation du service métier pour gérer les participations aux deals
 * Contient la logique métier (bff-core)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DealParticipantServiceImpl implements DealParticipantService {

    private final DealParticipantProvider dealParticipantProvider;

    @Override
    public DealParticipantModele ajouterParticipant(DealParticipantModele participation) {
        log.info("Ajout d'un participant au deal {} : utilisateur {}, {} parts",
                participation.getDealUuid(), participation.getUtilisateurUuid(), participation.getNombreDePart());

        // Validation métier
        validerParticipation(participation);

        return dealParticipantProvider.ajouterParticipant(participation);
    }

    @Override
    public DealParticipantModele mettreAJourNombreDePart(UUID dealUuid, UUID utilisateurUuid, Integer nombreDePart) {
        log.info("Mise à jour du nombre de parts : deal={}, utilisateur={}, nouvelles parts={}",
                dealUuid, utilisateurUuid, nombreDePart);

        // Validation métier
        if (dealUuid == null) {
            throw new IllegalArgumentException("L'UUID du deal est obligatoire");
        }
        if (utilisateurUuid == null) {
            throw new IllegalArgumentException("L'UUID de l'utilisateur est obligatoire");
        }
        if (nombreDePart == null || nombreDePart <= 0) {
            throw new IllegalArgumentException("Le nombre de parts doit être supérieur à 0");
        }

        return dealParticipantProvider.mettreAJourNombreDePart(dealUuid, utilisateurUuid, nombreDePart);
    }

    @Override
    public Optional<DealParticipantModele> trouverParticipation(UUID dealUuid, UUID utilisateurUuid) {
        log.debug("Recherche de la participation : deal={}, utilisateur={}", dealUuid, utilisateurUuid);

        if (dealUuid == null) {
            throw new IllegalArgumentException("L'UUID du deal est obligatoire");
        }
        if (utilisateurUuid == null) {
            throw new IllegalArgumentException("L'UUID de l'utilisateur est obligatoire");
        }

        return dealParticipantProvider.trouverParticipation(dealUuid, utilisateurUuid);
    }

    @Override
    public List<DealParticipantModele> trouverParticipantsParDeal(UUID dealUuid) {
        log.debug("Recherche de tous les participants du deal {}", dealUuid);

        if (dealUuid == null) {
            throw new IllegalArgumentException("L'UUID du deal est obligatoire");
        }

        return dealParticipantProvider.trouverParticipantsParDeal(dealUuid);
    }

    @Override
    public List<DealParticipantModele> trouverParticipantsParDealAvecUtilisateur(UUID dealUuid) {
        log.debug("Recherche de tous les participants du deal {} avec infos utilisateur", dealUuid);

        if (dealUuid == null) {
            throw new IllegalArgumentException("L'UUID du deal est obligatoire");
        }

        return dealParticipantProvider.trouverParticipantsParDealAvecUtilisateur(dealUuid);
    }

    @Override
    public List<DealParticipantModele> trouverParticipationsParUtilisateur(UUID utilisateurUuid) {
        log.debug("Recherche de toutes les participations de l'utilisateur {}", utilisateurUuid);

        if (utilisateurUuid == null) {
            throw new IllegalArgumentException("L'UUID de l'utilisateur est obligatoire");
        }

        return dealParticipantProvider.trouverParticipationsParUtilisateur(utilisateurUuid);
    }

    @Override
    public boolean utilisateurParticipeAuDeal(UUID dealUuid, UUID utilisateurUuid) {
        log.debug("Vérification si l'utilisateur {} participe au deal {}", utilisateurUuid, dealUuid);

        if (dealUuid == null) {
            throw new IllegalArgumentException("L'UUID du deal est obligatoire");
        }
        if (utilisateurUuid == null) {
            throw new IllegalArgumentException("L'UUID de l'utilisateur est obligatoire");
        }

        return dealParticipantProvider.utilisateurParticipeAuDeal(dealUuid, utilisateurUuid);
    }

    @Override
    public long compterParticipants(UUID dealUuid) {
        log.debug("Comptage des participants du deal {}", dealUuid);

        if (dealUuid == null) {
            throw new IllegalArgumentException("L'UUID du deal est obligatoire");
        }

        return dealParticipantProvider.compterParticipants(dealUuid);
    }

    @Override
    public long compterNombreParts(UUID dealUuid) {
        if (dealUuid == null) {
            throw new IllegalArgumentException("L'UUID du deal est obligatoire");
        }

        return dealParticipantProvider.compterNombreParts(dealUuid);
    }

    @Override
    public void supprimerParticipant(UUID dealUuid, UUID utilisateurUuid) {
        log.info("Suppression du participant : deal={}, utilisateur={}", dealUuid, utilisateurUuid);

        if (dealUuid == null) {
            throw new IllegalArgumentException("L'UUID du deal est obligatoire");
        }
        if (utilisateurUuid == null) {
            throw new IllegalArgumentException("L'UUID de l'utilisateur est obligatoire");
        }

        dealParticipantProvider.supprimerParticipant(dealUuid, utilisateurUuid);
    }

    @Override
    public void supprimerTousLesParticipants(UUID dealUuid) {
        log.info("Suppression de tous les participants du deal {}", dealUuid);

        if (dealUuid == null) {
            throw new IllegalArgumentException("L'UUID du deal est obligatoire");
        }

        dealParticipantProvider.supprimerTousLesParticipants(dealUuid);
    }

    /**
     * Valide une participation avant ajout
     * @param participation Participation à valider
     */
    private void validerParticipation(DealParticipantModele participation) {
        if (participation == null) {
            throw new IllegalArgumentException("La participation est obligatoire");
        }
        if (participation.getDealUuid() == null) {
            throw new IllegalArgumentException("L'UUID du deal est obligatoire");
        }
        if (participation.getUtilisateurUuid() == null) {
            throw new IllegalArgumentException("L'UUID de l'utilisateur est obligatoire");
        }
        if (participation.getNombreDePart() == null || participation.getNombreDePart() <= 0) {
            throw new IllegalArgumentException("Le nombre de parts doit être supérieur à 0");
        }
    }
}

