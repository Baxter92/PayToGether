package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.DealService;
import com.ulr.paytogether.core.domaine.service.SquarePaymentService;
import com.ulr.paytogether.core.domaine.service.UtilisateurService;
import com.ulr.paytogether.core.domaine.validator.AdresseValidator;
import com.ulr.paytogether.core.domaine.validator.PaiementValidator;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import com.ulr.paytogether.core.event.EventPublisher;
import com.ulr.paytogether.core.event.PaymentInitiatedEvent;
import com.ulr.paytogether.core.event.PaymentNotificationEvent;
import com.ulr.paytogether.core.event.PaymentRefundedEvent;
import com.ulr.paytogether.core.event.PaymentSuccessfulEvent;
import com.ulr.paytogether.core.exception.ResourceNotFoundException;
import com.ulr.paytogether.core.exception.ValidationException;
import com.ulr.paytogether.core.modele.AdresseModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.core.provider.AdresseProvider;
import com.ulr.paytogether.core.provider.CommandeProvider;
import com.ulr.paytogether.core.provider.DealParticipantProvider;
import com.ulr.paytogether.core.provider.PaiementProvider;
import com.ulr.paytogether.core.provider.SquarePaymentProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implémentation du service de paiement Square.
 * Gère les paiements via Square Payment API.
 * Les événements sont gérés par les handlers dans bff-api.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SquarePaymentServiceImpl implements SquarePaymentService {

    private final PaiementProvider paiementProvider;
    private final SquarePaymentProvider squarePaymentProvider;
    private final PaiementValidator paiementValidator;
    private final AdresseValidator adresseValidator;
    private final EventPublisher eventPublisher;
    private final AdresseProvider adresseProvider;
    private final CommandeProvider commandeProvider;
    private final DealParticipantProvider dealParticipantProvider;
    private final DealService dealService;
    private final UtilisateurService utilisateurService;

    @Transactional
    @Override
    public PaiementModele creerPaiementSquare(PaiementModele paiement) {
        log.info("Création paiement Square pour deal {}", paiement.getDeal().getUuid());

        // Validation métier
        paiementValidator.valider(paiement);
        adresseValidator.valider(paiement.getAdresse());

        // Initialiser avec statut EN_ATTENTE
        paiement.setStatut(StatutPaiement.EN_ATTENTE);

        // Sauvegarder le paiement en base
        PaiementModele paiementCree = paiementProvider.sauvegarder(paiement);

        log.info("Paiement Square créé avec UUID: {}", paiementCree.getUuid());

        PaymentInitiatedEvent paymentInitiatedEvent = PaymentInitiatedEvent
                .builder()
                .squareToken(paiementCree.getSquareToken())
                .methodePaiement(paiementCree.getMethodePaiement().name())
                .montant(paiementCree.getMontant())
                .commandeUuid(paiementCree.getCommande().getUuid())
                .paiementUuid(paiementCree.getUuid())
                .utilisateurUuid(paiementCree.getUtilisateur().getUuid())
                .nombreDePart(paiement.getNombreDePart())
                .build();

        eventPublisher.publishAsync(paymentInitiatedEvent);
        return paiementCree;
    }

    @Transactional
    @Override
    public PaiementModele traiterPaiementSquare(PaiementModele paiement) {
        log.info("Traitement du paiement Square UUID: {}", paiement.getUuid());

        try {
            // Valider la transition de statut
            paiementValidator.validerTransitionStatut(paiement.getStatut(), StatutPaiement.PROCESSING);

            // Mettre à jour le statut en PROCESSING
            paiement.setStatut(StatutPaiement.PROCESSING);
            PaiementModele paiementEnCours = paiementProvider.mettreAJour(paiement.getUuid(), paiement);

            // Appeler Square Payment API
            String squarePaymentId = squarePaymentProvider.creerPaiement(
                paiement.getSquareToken(),
                paiement.getMontant(),
                paiement.getSquareLocationId() != null ? paiement.getSquareLocationId() : "main",
                paiement.getCommande().getUuid().toString()
            );

            // Récupérer les détails du paiement
            SquarePaymentProvider.SquarePaymentDetails details =
                squarePaymentProvider.recupererDetailsPaiement(squarePaymentId);

            // Mettre à jour le paiement avec les informations Square
            paiementEnCours.setSquarePaymentId(details.paymentId());
            paiementEnCours.setSquareOrderId(details.orderId());
            paiementEnCours.setSquareLocationId(details.locationId());
            paiementEnCours.setSquareReceiptUrl(details.receiptUrl());
            paiementEnCours.setStatut(StatutPaiement.CONFIRME);
            paiementEnCours.setTransactionId(squarePaymentId);

            PaiementModele paiementFinal = paiementProvider.mettreAJour(
                paiementEnCours.getUuid(),
                paiementEnCours
            );

            log.info("Paiement Square traité avec succès: {}", squarePaymentId);
            PaymentSuccessfulEvent paymentSuccessfulEvent = PaymentSuccessfulEvent
                    .builder()
                    .paiementUuid(paiementFinal.getUuid())
                    .montant(paiementFinal.getMontant())
                    .squarePaymentId(squarePaymentId)
                    .commandeUuid(paiementFinal.getCommande().getUuid())
                    .squareReceiptUrl(paiementFinal.getSquareReceiptUrl())
                    .methodePaiement(paiementFinal.getMethodePaiement().name())
                    .utilisateurUuid(paiementFinal.getUtilisateur().getUuid())
                    .nombreDePart(paiement.getNombreDePart())
                    .build();

            eventPublisher.publishAsync(paymentSuccessfulEvent);
            return paiementFinal;

        } catch (Exception e) {
            log.error("Erreur traitement paiement Square: {}", e.getMessage(), e);

            // Mettre à jour le statut en ECHOUE
            paiement.setStatut(StatutPaiement.ECHOUE);
            paiement.setMessageErreur(e.getMessage());
            paiementProvider.mettreAJour(paiement.getUuid(), paiement);

            // NE PAS envoyer d'email ici - le handler avec @Retryable va réessayer
            // L'email d'échec sera envoyé UNIQUEMENT après épuisement des max attempts
            // via PaymentFailedEvent dans le handler @Recover

            throw new ValidationException("paiement.traitement.echec", e.getMessage());
        }
    }

    @Transactional
    @Override
    public PaiementModele verifierStatutPaiement(UUID paiementUuid) {
        log.info("Vérification du statut du paiement: {}", paiementUuid);

        PaiementModele paiement = paiementProvider.trouverParUuid(paiementUuid)
            .orElseThrow(() -> ResourceNotFoundException.parUuid("paiement", paiementUuid));

        if (paiement.getSquarePaymentId() == null) {
            throw new ValidationException("paiement.square.id.manquant");
        }

        // Vérifier le statut sur Square
        String statutSquare = squarePaymentProvider.verifierStatutPaiement(
            paiement.getSquarePaymentId()
        );

        // Mapper le statut Square vers notre énumération
        StatutPaiement nouveauStatut = mapperStatutSquare(statutSquare);

        if (nouveauStatut != paiement.getStatut()) {
            paiementValidator.validerTransitionStatut(paiement.getStatut(), nouveauStatut);
            paiement.setStatut(nouveauStatut);
            paiement = paiementProvider.mettreAJour(paiementUuid, paiement);
        }

        return paiement;
    }

    @Transactional
    @Override
    public PaiementModele rembourserPaiement(UUID paiementUuid) {
        log.info("Remboursement du paiement: {}", paiementUuid);

        PaiementModele paiement = paiementProvider.trouverParUuid(paiementUuid)
            .orElseThrow(() -> ResourceNotFoundException.parUuid("paiement", paiementUuid));

        // Validation métier pour le remboursement
        paiementValidator.validerRemboursement(paiement);

        if (paiement.getSquarePaymentId() == null) {
            throw new ValidationException("paiement.square.id.manquant");
        }

        try {
            // Rembourser sur Square
            String refundId = squarePaymentProvider.rembourserPaiement(
                paiement.getSquarePaymentId(),
                paiement.getMontant(),
                "Remboursement demandé par l'utilisateur"
            );

            // Mettre à jour le statut
            paiementValidator.validerTransitionStatut(paiement.getStatut(), StatutPaiement.REFUNDED);
            paiement.setStatut(StatutPaiement.REFUNDED);
            paiement.setMessageErreur("Remboursé - Refund ID: " + refundId);

            PaiementModele paiementRembourse = paiementProvider.mettreAJour(paiementUuid, paiement);

            log.info("Paiement remboursé avec succès: {}", refundId);
            return paiementRembourse;

        } catch (Exception e) {
            log.error("Erreur lors du remboursement: {}", e.getMessage(), e);
            throw new ValidationException("paiement.remboursement.echec", e.getMessage());
        }
    }

    @Override
    public void mettreAJourStatutCommandeDeal(UUID paiementUuid, String statut, int nombreDePart) {
        PaiementModele paiementModele = paiementProvider.mettreAJourStatutCommandeDeal(paiementUuid, statut, nombreDePart);

        AdresseModele adresseModele = adresseProvider.trouverParPaiement(paiementUuid);

        PaymentNotificationEvent paymentNotificationEvent = PaymentNotificationEvent
                .builder()
                .paiementUuid(paiementUuid)
                .utilisateurUuid(paiementModele.getUtilisateur().getUuid())
                .email(paiementModele.getUtilisateur().getEmail())
                .sujetNotification("Confirmation de paiement du deal " + paiementModele.getDeal().getTitre())
                .statutPaiement(statut)
                .typeNotification("EMAIL")
                .datePaiement(paiementModele.getDatePaiement())
                .methodePaiement(paiementModele.getMethodePaiement().name())
                .titreDeal(paiementModele.getDeal().getTitre())
                .descriptionDeal(paiementModele.getDeal().getDescription())
                .montantPaiement(paiementModele.getMontant())
                .nombreDePart(nombreDePart)
                .adresseRue(adresseModele.getRue())
                .adresseVille(adresseModele.getVille())
                .adresseProvince("")
                .adresseCodePostal(adresseModele.getCodePostal())
                .adressePays("")
                .build();

        eventPublisher.publishAsync(paymentNotificationEvent);
    }

    @Transactional
    @Override
    public int rembourserPaiementsEnMasse(List<UUID> utilisateurUuids, UUID dealUuid, String raisonRemboursement) {
        log.info("Remboursement en masse de {} utilisateurs pour le deal {}", utilisateurUuids.size(), dealUuid);

        int nombreRemboursements = 0;
        List<PaymentRefundedEvent> evenements = new ArrayList<>();

        DealModele deal = dealService.lireParUuid(dealUuid)
                .orElseThrow(() -> ResourceNotFoundException.parUuid("deal", dealUuid));

        for (UUID utilisateurUuid : utilisateurUuids) {
            try {
                // Trouver le paiement de l'utilisateur pour ce deal
                List<PaiementModele> paiements = paiementProvider.trouverParUtilisateur(utilisateurUuid);
                PaiementModele paiement = paiements.stream()
                        .filter(p -> p.getDeal() != null && p.getDeal().getUuid().equals(dealUuid))
                        .filter(p -> p.getStatut() == StatutPaiement.CONFIRME)
                        .findFirst()
                        .orElse(null);

                if (paiement == null) {
                    log.warn("Aucun paiement confirmé trouvé pour l'utilisateur {} sur le deal {}", utilisateurUuid, dealUuid);
                    continue;
                }

                // Rembourser via Square
                String refundId = squarePaymentProvider.refundPayment(paiement.getSquarePaymentId(), paiement.getMontant());

                // Mettre à jour le statut du paiement
                paiement.setStatut(StatutPaiement.REFUNDED);
                paiement.setMessageErreur("Remboursé: " + raisonRemboursement);
                paiementProvider.mettreAJour(paiement.getUuid(), paiement);

                // Récupérer l'utilisateur pour l'email
                UtilisateurModele utilisateur = utilisateurService.lireParUuid(utilisateurUuid)
                        .orElseThrow(() -> ResourceNotFoundException.parUuid("utilisateur", utilisateurUuid));

                // Créer l'événement de remboursement
                PaymentRefundedEvent event = new PaymentRefundedEvent(
                        paiement.getUuid(),
                        utilisateurUuid,
                        paiement.getCommande() != null ? paiement.getCommande().getUuid() : null,
                        dealUuid,
                        paiement.getMontant(),
                        refundId,
                        paiement.getNombreDePart(),
                        utilisateur.getEmail(),
                        utilisateur.getPrenom(),
                        utilisateur.getNom(),
                        deal.getTitre(),
                        deal.getDescription(),
                        LocalDateTime.now(),
                        raisonRemboursement
                );

                evenements.add(event);
                nombreRemboursements++;

                log.info("✅ Paiement {} remboursé avec succès (refundId: {})", paiement.getUuid(), refundId);

            } catch (Exception e) {
                log.error("❌ Erreur lors du remboursement pour l'utilisateur {}: {}", utilisateurUuid, e.getMessage(), e);
            }
        }

        // Publier tous les événements de remboursement
        evenements.forEach(eventPublisher::publishAsync);

        log.info("Remboursement en masse terminé: {}/{} succès", nombreRemboursements, utilisateurUuids.size());
        return nombreRemboursements;
    }

    @Transactional
    @Override
    public void supprimerParticipationApresRemboursement(UUID utilisateurUuid, UUID dealUuid, int nombreDeParts) {
        log.info("Suppression de la participation de l'utilisateur {} au deal {} ({} parts)",
                utilisateurUuid, dealUuid, nombreDeParts);

        try {
            // 1. Supprimer la participation au deal
            dealParticipantProvider.supprimerParticipation(utilisateurUuid, dealUuid);
            log.info("✅ Participation supprimée du deal");

            // 2. Trouver le paiement de cet utilisateur pour ce deal
            List<PaiementModele> paiements = paiementProvider.trouverParUtilisateur(utilisateurUuid);
            PaiementModele paiement = paiements.stream()
                    .filter(p -> p.getDeal() != null && p.getDeal().getUuid().equals(dealUuid))
                    .filter(p -> p.getStatut() == StatutPaiement.REFUNDED)
                    .findFirst()
                    .orElse(null);

            if (paiement != null) {
                UUID commandeUuid = paiement.getCommande() != null ? paiement.getCommande().getUuid() : null;

                // 3. Supprimer le paiement
                paiementProvider.supprimerParUuid(paiement.getUuid());
                log.info("✅ Paiement {} supprimé", paiement.getUuid());

                // 4. Vérifier si la commande a encore des paiements
                if (commandeUuid != null) {
                    List<PaiementModele> paiementsRestants = paiementProvider.trouverParCommande(commandeUuid);

                    if (paiementsRestants.isEmpty()) {
                        // Supprimer la commande si elle n'a plus de paiements
                        commandeProvider.supprimerParUuid(commandeUuid);
                        log.info("✅ Commande {} supprimée (plus de paiements)", commandeUuid);
                    } else {
                        log.info("Commande {} conservée ({} paiements restants)", commandeUuid, paiementsRestants.size());
                    }
                }
            }

            log.info("✅ Suppression de la participation terminée avec succès");

        } catch (Exception e) {
            log.error("❌ Erreur lors de la suppression de la participation: {}", e.getMessage(), e);
            throw new ValidationException("participation.suppression.echec", e.getMessage());
        }
    }


    /**
     * Mapper le statut Square vers notre énumération
     */
    private StatutPaiement mapperStatutSquare(String statutSquare) {
        return switch (statutSquare.toUpperCase()) {
            case "COMPLETED", "APPROVED" -> StatutPaiement.CONFIRME;
            case "PENDING" -> StatutPaiement.PROCESSING;
            case "FAILED", "DECLINED" -> StatutPaiement.ECHOUE;
            case "CANCELED" -> StatutPaiement.CANCELLED;
            default -> StatutPaiement.EN_ATTENTE;
        };
    }
}
