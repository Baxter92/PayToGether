package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.PubliciteService;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.PubliciteModele;
import com.ulr.paytogether.core.provider.PubliciteProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation du service pour les publicités
 * Contient la logique métier
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PubliciteServiceImpl implements PubliciteService {

    private final PubliciteProvider publiciteProvider;

    @Override
    public PubliciteModele creer(PubliciteModele publicite) {
        log.info("Création d'une publicité: {}", publicite.getTitre());
        return publiciteProvider.sauvegarder(publicite);
    }

    @Override
    public Optional<PubliciteModele> lireParUuid(UUID uuid) {
        log.debug("Lecture de la publicité: {}", uuid);
        return publiciteProvider.trouverParUuid(uuid);
    }

    @Override
    public List<PubliciteModele> lireTous() {
        log.debug("Lecture de toutes les publicités");
        return publiciteProvider.trouverTous();
    }

    @Override
    public List<PubliciteModele> lireActives() {
        log.debug("Lecture des publicités actives");
        return publiciteProvider.trouverActives();
    }

    @Override
    public PubliciteModele mettreAJour(UUID uuid, PubliciteModele publicite) {
        log.info("Mise à jour de la publicité: {}", uuid);
        return publiciteProvider.mettreAJour(uuid, publicite);
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        log.info("Suppression de la publicité: {}", uuid);
        publiciteProvider.supprimerParUuid(uuid);
    }

    @Override
    public void mettreAJourStatutImage(UUID publiciteUuid, UUID imageUuid, StatutImage statut) {
        publiciteProvider.mettreAJourStatutImage(publiciteUuid, imageUuid, statut);
    }

    @Override
    public String obtenirUrlLectureImage(UUID publiciteUuid, UUID imageUuid) {
        return publiciteProvider.obtenirUrlLectureImage(publiciteUuid, imageUuid);
    }
}
