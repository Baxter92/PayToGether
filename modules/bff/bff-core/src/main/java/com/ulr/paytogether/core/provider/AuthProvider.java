package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.modele.LoginModele;
import com.ulr.paytogether.core.modele.LoginResponseModele;
import com.ulr.paytogether.core.modele.MeResponseModele;

/**
 * Port (interface) pour les opérations d'authentification
 * Cette interface définit le contrat que l'adaptateur (provider) doit implémenter
 * Architecture Hexagonale : c'est un port de sortie (driven port)
 */
public interface AuthProvider {

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

