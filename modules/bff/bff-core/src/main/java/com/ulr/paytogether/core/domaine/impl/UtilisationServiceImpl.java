package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.UtilisateurService;
import com.ulr.paytogether.core.domaine.validator.UtilisateurValidator;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.core.provider.UtilisateurProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UtilisationServiceImpl implements UtilisateurService {

    private final UtilisateurProvider utilisateurProvider;
    private final UtilisateurValidator utilisateurValidator;

    @Override
    public UtilisateurModele creer(UtilisateurModele utilisateur) {
        // Validation de l'utilisateur avant création
        utilisateurValidator.validerPourCreation(utilisateur);

        return utilisateurProvider.sauvegarder(utilisateur);
    }

    @Override
    public Optional<UtilisateurModele> lireParUuid(UUID uuid) {
        return utilisateurProvider.trouverParUuid(uuid);
    }

    @Override
    public Optional<UtilisateurModele> lireParEmail(String email) {
        return utilisateurProvider.trouverParEmail(email);
    }

    @Override
    public List<UtilisateurModele> lireTous() {
        return utilisateurProvider.trouverTous();
    }

    @Override
    public UtilisateurModele mettreAJour(UUID uuid, UtilisateurModele utilisateur) {
        // Validation de l'utilisateur avant mise à jour
        utilisateurValidator.validerPourMiseAJour(utilisateur);

        return utilisateurProvider.mettreAJour(uuid, utilisateur);
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        utilisateurProvider.supprimerParUuid(uuid);
    }

    @Override
    public boolean existeParEmail(String email) {
        return utilisateurProvider.existeParEmail(email);
    }
}
