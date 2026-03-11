package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.modele.DealParticipantModele;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface Provider pour gérer les participations aux deals
 * Port de l'architecture hexagonale
 */
public interface DealParticipantProvider {

    /**
     * Ajoute un participant à un deal
     * @param participation Participation à ajouter
     * @return Participation sauvegardée
     */
    DealParticipantModele ajouterParticipant(DealParticipantModele participation);

    /**
     * Met à jour le nombre de parts d'un participant
     * @param dealUuid UUID du deal
     * @param utilisateurUuid UUID de l'utilisateur
     * @param nombreDePart Nouveau nombre de parts
     * @return Participation mise à jour
     */
    DealParticipantModele mettreAJourNombreDePart(UUID dealUuid, UUID utilisateurUuid, Integer nombreDePart);

    /**
     * Trouve une participation spécifique
     * @param dealUuid UUID du deal
     * @param utilisateurUuid UUID de l'utilisateur
     * @return Participation si elle existe
     */
    Optional<DealParticipantModele> trouverParticipation(UUID dealUuid, UUID utilisateurUuid);

    /**
     * Trouve tous les participants d'un deal
     * @param dealUuid UUID du deal
     * @return Liste des participants
     */
    List<DealParticipantModele> trouverParticipantsParDeal(UUID dealUuid);

    /**
     * Trouve tous les participants d'un deal avec les informations utilisateur complètes
     * @param dealUuid UUID du deal
     * @return Liste des participants enrichis
     */
    List<DealParticipantModele> trouverParticipantsParDealAvecUtilisateur(UUID dealUuid);

    /**
     * Trouve toutes les participations d'un utilisateur
     * @param utilisateurUuid UUID de l'utilisateur
     * @return Liste des participations
     */
    List<DealParticipantModele> trouverParticipationsParUtilisateur(UUID utilisateurUuid);

    /**
     * Vérifie si un utilisateur participe déjà à un deal
     * @param dealUuid UUID du deal
     * @param utilisateurUuid UUID de l'utilisateur
     * @return true si l'utilisateur participe
     */
    boolean utilisateurParticipeAuDeal(UUID dealUuid, UUID utilisateurUuid);

    /**
     * Compte le nombre de participants d'un deal
     * @param dealUuid UUID du deal
     * @return Nombre de participants
     */
    long compterParticipants(UUID dealUuid);

    /**
     * Compte le nombre de parts d'un deal
     * @param dealUuid UUID du deal
     * @return Nombre de participants
     */
    long compterNombreParts(UUID dealUuid);

    /**
     * Supprime un participant d'un deal
     * @param dealUuid UUID du deal
     * @param utilisateurUuid UUID de l'utilisateur
     */
    void supprimerParticipant(UUID dealUuid, UUID utilisateurUuid);

    /**
     * Supprime une participation (alias de supprimerParticipant)
     * @param utilisateurUuid UUID de l'utilisateur
     * @param dealUuid UUID du deal
     */
    default void supprimerParticipation(UUID utilisateurUuid, UUID dealUuid) {
        supprimerParticipant(dealUuid, utilisateurUuid);
    }

    /**
     * Supprime tous les participants d'un deal
     * @param dealUuid UUID du deal
     */
    void supprimerTousLesParticipants(UUID dealUuid);
}

