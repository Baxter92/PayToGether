package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.SquarePaymentService;
import com.ulr.paytogether.core.domaine.validator.PaiementValidator;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import com.ulr.paytogether.core.event.EventPublisher;
import com.ulr.paytogether.core.event.PaymentInitiatedEvent;
import com.ulr.paytogether.core.event.PaymentNotificationEvent;
import com.ulr.paytogether.core.event.PaymentSuccessfulEvent;
import com.ulr.paytogether.core.exception.ResourceNotFoundException;
import com.ulr.paytogether.core.exception.ValidationException;
import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.core.provider.PaiementProvider;
import com.ulr.paytogether.core.provider.SquarePaymentProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final EventPublisher eventPublisher;

    @Override
    public PaiementModele creerPaiementSquare(PaiementModele paiement) {
        log.info("Création paiement Square pour deal {}", paiement.getDeal().getUuid());

        // Validation métier
        paiementValidator.valider(paiement);

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
                .utilisateurUuid(paiementCree.getUtilisateur().getUuid())
                .build();

        eventPublisher.publishAsync(paymentInitiatedEvent);
        return paiementCree;
    }

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
                    .build();

            eventPublisher.publishAsync(paymentSuccessfulEvent);
            return paiementFinal;

        } catch (Exception e) {
            log.error("Erreur traitement paiement Square: {}", e.getMessage(), e);

            // Mettre à jour le statut en ECHOUE
            paiement.setStatut(StatutPaiement.ECHOUE);
            paiement.setMessageErreur(e.getMessage());
            paiementProvider.mettreAJour(paiement.getUuid(), paiement);

            PaymentNotificationEvent paymentNotificationEvent = PaymentNotificationEvent
                    .builder()
                    .paiementUuid(paiement.getUuid())
                    .utilisateurUuid(paiement.getUtilisateur().getUuid())
                    .email(paiement.getUtilisateur().getEmail())
                    .sujetNotification("Échec du paiement du deal " + paiement.getDeal().getTitre())
                    .statutPaiement(StatutPaiement.ECHOUE.name())
                    .typeNotification("EMAIL")
                    .datePaiement(paiement.getDatePaiement())
                    .methodePaiement(paiement.getMethodePaiement().name())
                    .titreDeal(paiement.getDeal().getTitre())
                    .descriptionDeal(paiement.getDeal().getDescription())
                    .montantPaiement(paiement.getMontant())
                    .build();

            eventPublisher.publishAsync(paymentNotificationEvent);

            throw new ValidationException("paiement.traitement.echec", e.getMessage());
        }
    }

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
    public void mettreAJourStatutCommandeDeal(UUID paiementUuid, String statut) {
        PaiementModele paiementModele = paiementProvider.mettreAJourStatutCommandeDeal(paiementUuid, statut);
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
                .build();

        eventPublisher.publishAsync(paymentNotificationEvent);
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
