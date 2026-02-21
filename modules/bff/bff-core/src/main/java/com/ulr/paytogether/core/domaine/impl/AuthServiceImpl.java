package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.AuthService;
import com.ulr.paytogether.core.modele.LoginModele;
import com.ulr.paytogether.core.modele.LoginResponseModele;
import com.ulr.paytogether.core.modele.MeResponseModele;
import com.ulr.paytogether.core.provider.AuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implémentation du service d'authentification
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthProvider authProvider;

    @Override
    public LoginResponseModele login(LoginModele login) {
        log.info("Authentification de l'utilisateur: {}", login.getUsername());

        if (login.getUsername() == null || login.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur ne peut pas être vide");
        }

        if (login.getPassword() == null || login.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide");
        }

        return authProvider.login(login);
    }

    @Override
    public MeResponseModele getMe(String token) {
        log.debug("Récupération des informations de l'utilisateur connecté");

        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Le token ne peut pas être vide");
        }

        return authProvider.getMe(token);
    }

    @Override
    public void logout(String token) {
        log.info("Déconnexion de l'utilisateur");

        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Le token ne peut pas être vide");
        }

        authProvider.logout(token);
    }
}

