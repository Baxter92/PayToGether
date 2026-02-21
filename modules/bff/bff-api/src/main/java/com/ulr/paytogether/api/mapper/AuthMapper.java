package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.LoginDTO;
import com.ulr.paytogether.api.dto.LoginResponseDTO;
import com.ulr.paytogether.api.dto.MeResponseDTO;
import com.ulr.paytogether.core.modele.LoginModele;
import com.ulr.paytogether.core.modele.LoginResponseModele;
import com.ulr.paytogether.core.modele.MeResponseModele;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre DTO et Modèle d'authentification
 */
@Component
public class AuthMapper {

    /**
     * Convertit LoginDTO en LoginModele
     */
    public LoginModele dtoVersModele(LoginDTO dto) {
        if (dto == null) {
            return null;
        }

        return LoginModele.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .build();
    }

    /**
     * Convertit LoginResponseModele en LoginResponseDTO
     */
    public LoginResponseDTO modeleVersDto(LoginResponseModele modele) {
        if (modele == null) {
            return null;
        }

        return LoginResponseDTO.builder()
                .accessToken(modele.getAccessToken())
                .refreshToken(modele.getRefreshToken())
                .tokenType(modele.getTokenType())
                .expiresIn(modele.getExpiresIn())
                .refreshExpiresIn(modele.getRefreshExpiresIn())
                .scope(modele.getScope())
                .build();
    }

    /**
     * Convertit MeResponseModele en MeResponseDTO
     */
    public MeResponseDTO modeleVersDto(MeResponseModele modele) {
        if (modele == null) {
            return null;
        }

        return MeResponseDTO.builder()
                .id(modele.getId())
                .username(modele.getUsername())
                .email(modele.getEmail())
                .prenom(modele.getPrenom())
                .nom(modele.getNom())
                .actif(modele.isActif())
                .emailVerifie(modele.isEmailVerifie())
                .dateCreationTimestamp(modele.getDateCreationTimestamp())
                .roles(modele.getRoles())
                .build();
    }
}

