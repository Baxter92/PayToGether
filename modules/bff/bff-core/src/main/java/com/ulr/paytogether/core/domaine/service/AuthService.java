package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.modele.LoginModele;
import com.ulr.paytogether.core.modele.LoginResponseModele;
import com.ulr.paytogether.core.modele.MeResponseModele;

/**
 * Service métier pour l'authentification
 */
public interface AuthService {

    /**
     * Authentifier un utilisateur
     * @param login informations de connexion
     * @return réponse avec les tokens
     */
    LoginResponseModele login(LoginModele login);

    /**
     * Obtenir les informations de l'utilisateur connecté
     * @param token token JWT
     * @return informations de l'utilisateur
     */
    MeResponseModele getMe(String token);

    /**
     * Déconnecter un utilisateur
     * @param token token JWT
     */
    void logout(String token);
}

