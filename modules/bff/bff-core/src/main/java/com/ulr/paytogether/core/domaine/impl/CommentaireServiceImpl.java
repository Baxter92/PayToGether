package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.CommentaireService;
import com.ulr.paytogether.core.modele.CommentaireModele;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommentaireServiceImpl implements CommentaireService {

    @Override
    public CommentaireModele creer(CommentaireModele commentaireModele) {
        return null;
    }

    @Override
    public Optional<CommentaireModele> lireParUuid(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public List<CommentaireModele> lireTous(UUID uuid) {
        return List.of();
    }

    @Override
    public CommentaireModele mettreAJour(UUID uuid, CommentaireModele paiement) {
        return null;
    }

    @Override
    public void supprimerParUuid(UUID uuid) {

    }
}