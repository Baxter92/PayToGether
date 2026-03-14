package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.UtilisateurService;
import com.ulr.paytogether.core.domaine.validator.ActivationCompteValidator;
import com.ulr.paytogether.core.domaine.validator.ReinitialiserMotDePasseValidator;
import com.ulr.paytogether.core.domaine.validator.UtilisateurValidator;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.event.AccountValidationEvent;
import com.ulr.paytogether.core.event.EventPublisher;
import com.ulr.paytogether.core.event.PasswordResetEvent;
import com.ulr.paytogether.core.modele.MarchandAvecDealsModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.core.modele.ValidationTokenModele;
import com.ulr.paytogether.core.provider.UtilisateurProvider;
import com.ulr.paytogether.core.provider.ValidationTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final UtilisateurValidator utilisateurValidator;
    private final EventPublisher eventDispatcher;
    private final ValidationTokenProvider validationTokenProvider;
    private final ActivationCompteValidator activationCompteValidator;
    private final ReinitialiserMotDePasseValidator reinitialiserMotDePasseValidator;

    @Override
    public UtilisateurModele creer(UtilisateurModele utilisateur) {
        log.info("Création d'un utilisateur: {}", utilisateur.getEmail());

        utilisateurValidator.validerPourCreation(utilisateur);

        UtilisateurModele cree = utilisateurProvider.sauvegarder(utilisateur);

        // Dispatcher l'événement de validation de compte
        try {
            String token = genererToken();
            LocalDateTime expiration = LocalDateTime.now().plusHours(24);

            AccountValidationEvent event = new AccountValidationEvent(
                cree.getUuid(),
                cree.getEmail(),
                cree.getPrenom(),
                cree.getNom(),
                token,
                expiration
            );

            eventDispatcher.publishAsync(event);
            log.info("Événement de validation de compte dispatché pour: {}", cree.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors du dispatch de l'événement de validation: {}", e.getMessage(), e);
            // On ne propage pas l'erreur pour ne pas bloquer la création du compte
        }

        return cree;
    }

    /**
     * Génère un token unique pour la validation de compte
     */
    private String genererToken() {
        return UUID.randomUUID().toString().replace("-", "");
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
    public List<UtilisateurModele> lireTousMarchands() {
        log.debug("Lecture de tous les marchands");
        return utilisateurProvider.trouverTousMarchands();
    }

    @Override
    public UtilisateurModele mettreAJour(UUID uuid, UtilisateurModele utilisateur, String token) {
        log.info("Mise à jour de l'utilisateur: {}, token: {}", uuid, token);
        utilisateurValidator.validerPourMiseAJour(utilisateur, uuid);
        return utilisateurProvider.mettreAJour(uuid, utilisateur, token);
    }

    @Override
    public void supprimerParUuid(UUID uuid, String token) {
        log.info("Suppression de l'utilisateur: {}", uuid);
        utilisateurProvider.supprimerParUuid(uuid, token);
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
    public void demanderReinitialisationMotDePasse(String email) {
        log.info("Demande de réinitialisation de mot de passe pour: {}", email);

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email ne peut pas être vide");
        }

        // Récupérer l'utilisateur par email
        UtilisateurModele utilisateur = utilisateurProvider.trouverParEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Aucun utilisateur trouvé avec cet email"));

        // Générer un token de réinitialisation
        String token = genererToken();
        LocalDateTime expiration = LocalDateTime.now().plusHours(1); // Token valide 1h

        // Publier l'événement PasswordResetEvent
        try {
            PasswordResetEvent event = new PasswordResetEvent(
                    utilisateur.getUuid(),
                    utilisateur.getEmail(),
                    utilisateur.getPrenom(),
                    utilisateur.getNom(),
                    token,
                    expiration
            );

            eventDispatcher.publishAsync(event);
            log.info("Événement PasswordResetEvent dispatché pour: {}", utilisateur.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors du dispatch de l'événement de réinitialisation: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email de réinitialisation", e);
        }
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

    @Override
    public List<MarchandAvecDealsModele> lireTousMarchandsAvecDeals() {
        log.info("Récupération de tous les marchands avec leurs deals enrichis");
        return utilisateurProvider.trouverTousMarchandsAvecDeals();
    }

    @Override
    public void activerCompteAvecToken(String token) {
        log.info("Service - Activation de compte avec token");

        // 1. Validation métier du token
        activationCompteValidator.valider(token);

        // 2. Récupérer le token en base
        ValidationTokenModele tokenModele = validationTokenProvider.trouverParToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide ou expiré"));

        // 3. Vérifier que le token n'a pas été utilisé
        if (tokenModele.getUtilise()) {
            throw new IllegalArgumentException("Ce token a déjà été utilisé");
        }

        // 4. Vérifier que le token n'est pas expiré
        if (tokenModele.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Ce token a expiré");
        }

        // 5. Vérifier le type de token
        if (!"VALIDATION_COMPTE".equals(tokenModele.getTypeToken())) {
            throw new IllegalArgumentException("Type de token invalide");
        }

        // 6. Activer le compte utilisateur
        UUID utilisateurUuid = tokenModele.getUtilisateurUuid();
        utilisateurProvider.activerUtilisateur(utilisateurUuid, true, token);

        // 7. Marquer le token comme utilisé
        validationTokenProvider.marquerCommeUtilise(token);

        log.info("Compte activé avec succès pour l'utilisateur: {}", utilisateurUuid);
    }

    @Override
    public void reinitialiserMotDePasseAvecToken(String token, String nouveauMotDePasse) {
        log.info("Service - Réinitialisation de mot de passe avec token");

        // 1. Validation métier
        reinitialiserMotDePasseValidator.valider(token, nouveauMotDePasse);

        // 2. Récupérer le token en base
        ValidationTokenModele tokenModele = validationTokenProvider.trouverParToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide ou expiré"));

        // 3. Vérifier que le token n'a pas été utilisé
        if (tokenModele.getUtilise()) {
            throw new IllegalArgumentException("Ce token a déjà été utilisé");
        }

        // 4. Vérifier que le token n'est pas expiré
        if (tokenModele.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Ce token a expiré");
        }

        // 5. Vérifier le type de token
        if (!"REINITIALISATION_MOT_DE_PASSE".equals(tokenModele.getTypeToken())) {
            throw new IllegalArgumentException("Type de token invalide");
        }

        // 6. Réinitialiser le mot de passe
        UUID utilisateurUuid = tokenModele.getUtilisateurUuid();
        utilisateurProvider.reinitialiserMotDePasse(utilisateurUuid, nouveauMotDePasse, token);

        // 7. Marquer le token comme utilisé
        validationTokenProvider.marquerCommeUtilise(token);

        log.info("Mot de passe réinitialisé avec succès pour l'utilisateur: {}", utilisateurUuid);
    }
}
