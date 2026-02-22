package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.UtilisateurService;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.core.provider.UtilisateurProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation du service Utilisateur
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurProvider utilisateurProvider;

    @Override
    public UtilisateurModele creer(UtilisateurModele utilisateur) {
        log.info("Création d'un utilisateur: {}", utilisateur.getEmail());

        if (utilisateurProvider.existeParEmail(utilisateur.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        return utilisateurProvider.sauvegarder(utilisateur);
    }

    @Override
    public Optional<UtilisateurModele> lireParUuid(UUID uuid) {
        log.debug("Lecture de l'utilisateur: {}", uuid);
        return utilisateurProvider.trouverParUuid(uuid);
    }

    @Override
    public Optional<UtilisateurModele> lireParEmail(String email) {
        log.debug("Lecture de l'utilisateur par email: {}", email);
        return utilisateurProvider.trouverParEmail(email);
    }

    @Override
    public List<UtilisateurModele> lireTous() {
        log.debug("Lecture de tous les utilisateurs");
        return utilisateurProvider.trouverTous();
    }

    @Override
    public UtilisateurModele mettreAJour(UUID uuid, UtilisateurModele utilisateur, String token) {
        log.info("Mise à jour de l'utilisateur: {}, token: {}", uuid, token);
        return utilisateurProvider.mettreAJour(uuid, utilisateur, token);
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        log.info("Suppression de l'utilisateur: {}", uuid);
        utilisateurProvider.supprimerParUuid(uuid);
    }

    @Override
    public boolean existeParEmail(String email) {
        log.debug("Vérification de l'existence de l'email: {}", email);
        return utilisateurProvider.existeParEmail(email);
    }

    @Override
    public void mettreAJourStatutPhotoProfil(UUID utilisateurUuid, StatutImage statut) {
        utilisateurProvider.mettreAJourStatutPhotoProfil(utilisateurUuid, statut);
    }

    @Override
    public String obtenirUrlLecturePhotoProfil(UUID utilisateurUuid) {
        return utilisateurProvider.obtenirUrlLecturePhotoProfil(utilisateurUuid);
    }

    @Override
    public void reinitialiserMotDePasse(UUID utilisateurUuid, String nouveauMotDePasse, String token) {
        log.info("Réinitialisation du mot de passe pour l'utilisateur: {}", utilisateurUuid);

        if (nouveauMotDePasse == null || nouveauMotDePasse.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nouveau mot de passe ne peut pas être vide");
        }

        utilisateurProvider.reinitialiserMotDePasse(utilisateurUuid, nouveauMotDePasse, token);
    }

    @Override
    public void activerUtilisateur(UUID utilisateurUuid, boolean actif, String token) {
        log.info("Activation/Désactivation de l'utilisateur: {} - actif: {}", utilisateurUuid, actif);
        utilisateurProvider.activerUtilisateur(utilisateurUuid, actif, token);
    }

    @Override
    public void assignerRole(UUID utilisateurUuid, String nomRole, String token) {
        log.info("Assignation du rôle {} à l'utilisateur: {}", nomRole, utilisateurUuid);

        if (nomRole == null || nomRole.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du rôle ne peut pas être vide");
        }

        utilisateurProvider.assignerRole(utilisateurUuid, nomRole, token);
    }
}
