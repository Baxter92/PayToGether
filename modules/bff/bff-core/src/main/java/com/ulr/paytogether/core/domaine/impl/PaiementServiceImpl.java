package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.PaiementService;
import com.ulr.paytogether.core.domaine.validator.PaiementValidator;
import com.ulr.paytogether.core.exception.ResourceNotFoundException;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import com.ulr.paytogether.core.provider.PaiementProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.UUID;

/**
 * Implémentation du service Paiement
 * Gère toute la logique métier des paiements
 * Support Square Payment avec validation complète
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaiementServiceImpl implements PaiementService {

    private final PaiementProvider paiementProvider;
    private final PaiementValidator paiementValidator;

    @Override
    public PaiementModele creer(PaiementModele paiement) {
        log.info("Création d'un paiement pour le deal: {}",
                paiement.getDeal() != null ? paiement.getDeal().getUuid() : "null");

        // ✅ OBLIGATOIRE : Validation avant création
        paiementValidator.valider(paiement);

        PaiementModele paiementCree = paiementProvider.sauvegarder(paiement);
        log.info("Paiement créé avec succès: {}", paiementCree.getUuid());

        return paiementCree;
    }

    @Override
    public Optional<PaiementModele> lireParUuid(UUID uuid) {
        log.debug("Lecture du paiement: {}", uuid);
        return paiementProvider.trouverParUuid(uuid);
    }

    @Override
    public List<PaiementModele> lireTous() {
        log.debug("Lecture de tous les paiements");
        return paiementProvider.trouverTous();
    }

    @Override
    public List<PaiementModele> lireParStatut(StatutPaiement statut) {
        log.debug("Lecture des paiements avec statut: {}", statut);

        // Utilisation d'une méthode alternative car trouverParStatut n'existe pas dans le provider
        // On filtre côté service
        return paiementProvider.trouverTous().stream()
                .filter(p -> p.getStatut() == statut)
                .toList();
    }

    @Override
    public List<PaiementModele> lireParDeal(UUID dealUuid) {
        log.debug("Lecture des paiements du deal: {}", dealUuid);

        // Utilisation d'une méthode alternative car trouverParDeal n'existe pas dans le provider
        // On filtre côté service
        return paiementProvider.trouverTous().stream()
                .filter(p -> p.getDeal() != null && p.getDeal().getUuid().equals(dealUuid))
                .toList();
    }

    @Override
    public List<PaiementModele> trouverParCommande(UUID commandeUuid) {
        log.debug("Recherche des paiements de la commande: {}", commandeUuid);
        return paiementProvider.trouverParCommande(commandeUuid);
    }

    @Override
    public DealModele mettreAJour(UUID uuid, PaiementModele paiement) {
        log.info("Mise à jour du paiement: {}", uuid);

        // Vérifier que le paiement existe
        paiementProvider.trouverParUuid(uuid)
                .orElseThrow(() -> ResourceNotFoundException.parUuid("paiement", uuid));

        // ✅ OBLIGATOIRE : Validation avant mise à jour
        paiement.setUuid(uuid);
        paiementValidator.validerPourMiseAJour(paiement);

        // Mise à jour via le provider
        PaiementModele paiementMisAJour = paiementProvider.mettreAJour(uuid, paiement);
        log.info("Paiement mis à jour avec succès: {}", uuid);

        // ⚠️ NOTE : Le type de retour devrait être PaiementModele, pas DealModele
        // Pour l'instant, on retourne le deal associé au paiement
        return paiementMisAJour.getDeal();
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        log.info("Suppression du paiement: {}", uuid);

        // Vérifier que le paiement existe avant de supprimer
        paiementProvider.trouverParUuid(uuid)
                .orElseThrow(() -> ResourceNotFoundException.parUuid("paiement", uuid));

        paiementProvider.supprimerParUuid(uuid);
        log.info("Paiement supprimé avec succès: {}", uuid);
    }

    @Override
    public List<PaiementModele> lireTousAvecInfosCompletes() {
        log.info("Récupération de tous les paiements avec informations complètes");
        return paiementProvider.trouverTousAvecInfosCompletes();
    }

    @Override
    public Map<String, Object> calculerStatistiques() {
        log.info("Calcul des statistiques des paiements");
        return paiementProvider.calculerStatistiquesPaiements();
    }

    @Override
    public List<PaiementModele> lireParUtilisateurAvecInfosCompletes(String keycloakId) {
        log.info("Récupération des paiements de l'utilisateur {} avec informations complètes", keycloakId);
        return paiementProvider.trouverParUtilisateurAvecInfosCompletes(keycloakId);
    }
}

