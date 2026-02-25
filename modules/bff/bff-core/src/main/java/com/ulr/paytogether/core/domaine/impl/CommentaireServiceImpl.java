package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.CommentaireService;
import com.ulr.paytogether.core.domaine.validator.CommentaireValidator;
import com.ulr.paytogether.core.modele.CommentaireModele;
import com.ulr.paytogether.core.provider.CommentaireProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentaireServiceImpl implements CommentaireService {

    private final CommentaireProvider commentaireProvider;
    private final CommentaireValidator commentaireValidator;

    @Override
    public CommentaireModele creer(CommentaireModele commentaireModele) {
        // Validation métier avant création
        commentaireValidator.valider(commentaireModele);

        return commentaireProvider.sauvegarder(commentaireModele);
    }

    @Override
    public Optional<CommentaireModele> lireParUuid(UUID uuid) {
        return commentaireProvider.trouverParUuid(uuid);
    }

    @Override
    public List<CommentaireModele> lireTous(UUID dealUuid) {
        return commentaireProvider.trouverParDeal(dealUuid);
    }

    @Override
    public CommentaireModele mettreAJour(UUID uuid, CommentaireModele commentaire) {
        // Validation métier avant mise à jour
        commentaireValidator.validerPourMiseAJour(commentaire);

        return commentaireProvider.sauvegarder(commentaire);
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        commentaireProvider.supprimerParUuid(uuid);
    }
}

