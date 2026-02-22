package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.LoginDTO;
import com.ulr.paytogether.api.dto.LoginResponseDTO;
import com.ulr.paytogether.api.dto.MeResponseDTO;
import com.ulr.paytogether.api.mapper.AuthMapper;
import com.ulr.paytogether.core.domaine.service.AuthService;
import com.ulr.paytogether.core.modele.LoginModele;
import com.ulr.paytogether.core.modele.LoginResponseModele;
import com.ulr.paytogether.core.modele.MeResponseModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ApiAdapter pour l'authentification
 * Fait le pont entre les Resources (API REST) et le Service Core
 * Gère la conversion entre DTO (API) et Modèle (Core)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthApiAdapter {

    private final AuthService authService;
    private final AuthMapper mapper;

    /**
     * Authentifier un utilisateur
     */
    public LoginResponseDTO login(LoginDTO loginDTO) {
        log.info("ApiAdapter - Authentification de l'utilisateur: {}", loginDTO.getUsername());

        LoginModele modele = mapper.dtoVersModele(loginDTO);
        LoginResponseModele reponse = authService.login(modele);

        return mapper.modeleVersDto(reponse);
    }

    /**
     * Obtenir les informations de l'utilisateur connecté
     */
    public MeResponseDTO getMe(String token) {
        log.debug("ApiAdapter - Récupération des informations de l'utilisateur connecté");

        MeResponseModele modele = authService.getMe(token);

        return mapper.modeleVersDto(modele);
    }

    /**
     * Déconnecter un utilisateur
     */
    public void logout(String token) {
        log.info("ApiAdapter - Déconnexion de l'utilisateur");

        authService.logout(token);
    }
}

