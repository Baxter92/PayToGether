package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.ValidationTokenService;
import com.ulr.paytogether.core.modele.ValidationTokenModele;
import com.ulr.paytogether.core.provider.ValidationTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation du service de gestion des tokens de validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationTokenServiceImpl implements ValidationTokenService {

    private final ValidationTokenProvider validationTokenProvider;

    @Override
    public ValidationTokenModele creer(ValidationTokenModele tokenModele) {
        log.info("Création d'un token de validation pour l'utilisateur: {}",
                tokenModele.getUtilisateurUuid());
        return validationTokenProvider.sauvegarder(tokenModele);
    }

    @Override
    public Optional<ValidationTokenModele> trouverParToken(String token) {
        log.debug("Recherche du token: {}", token);
        return validationTokenProvider.trouverParToken(token);
    }

    @Override
    public Optional<ValidationTokenModele> trouverParUtilisateur(UUID utilisateurUuid) {
        log.debug("Recherche du token pour l'utilisateur: {}", utilisateurUuid);
        return validationTokenProvider.trouverParUtilisateur(utilisateurUuid);
    }

    @Override
    public void marquerCommeUtilise(String token) {
        log.info("Marquage du token comme utilisé: {}", token);
        validationTokenProvider.marquerCommeUtilise(token);
    }

    @Override
    public int supprimerTokensExpires() {
        log.info("Suppression des tokens expirés");
        return validationTokenProvider.supprimerTokensExpires();
    }
}

