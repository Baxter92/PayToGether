package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.modele.ValidationTokenModele;
import com.ulr.paytogether.core.provider.ValidationTokenProvider;
import com.ulr.paytogether.provider.adapter.entity.ValidationTokenJpa;
import com.ulr.paytogether.provider.adapter.mapper.ValidationTokenJpaMapper;
import com.ulr.paytogether.provider.repository.ValidationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptateur pour les tokens de validation (implémentation du port ValidationTokenProvider)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationTokenProviderAdapter implements ValidationTokenProvider {

    private final ValidationTokenRepository tokenRepository;
    private final ValidationTokenJpaMapper mapper;

    @Override
    public Optional<ValidationTokenModele> trouverParToken(String token) {
        log.debug("Provider - Recherche du token: {}", token);
        return tokenRepository.findByToken(token)
                .map(mapper::versModele);
    }

    @Override
    public Optional<ValidationTokenModele> trouverParUtilisateur(UUID utilisateurUuid) {
        log.debug("Provider - Recherche du token pour l'utilisateur: {}", utilisateurUuid);
        return tokenRepository.findByUtilisateurUuid(utilisateurUuid)
                .map(mapper::versModele);
    }

    @Override
    @Transactional
    public void marquerCommeUtilise(String token) {
        log.debug("Provider - Marquage du token comme utilisé: {}", token);
        ValidationTokenJpa tokenJpa = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token non trouvé: " + token));

        tokenJpa.setUtilise(true);
        // dateModification est gérée automatiquement par @UpdateTimestamp
        tokenRepository.save(tokenJpa);
    }

    @Override
    @Transactional
    public ValidationTokenModele sauvegarder(ValidationTokenModele token) {
        log.debug("Provider - Sauvegarde du token");
        ValidationTokenJpa tokenJpa = mapper.versEntite(token);
        ValidationTokenJpa sauvegarde = tokenRepository.save(tokenJpa);
        return mapper.versModele(sauvegarde);
    }

    @Override
    @Transactional
    public int supprimerTokensExpires() {
        log.debug("Provider - Suppression des tokens expirés");
        LocalDateTime maintenant = LocalDateTime.now();
        int nombreSupprime = tokenRepository.deleteByDateExpirationBefore(maintenant);
        log.info("Provider - {} tokens expirés supprimés", nombreSupprime);
        return nombreSupprime;
    }
}

