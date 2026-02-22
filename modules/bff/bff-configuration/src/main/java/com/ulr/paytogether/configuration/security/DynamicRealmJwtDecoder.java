package com.ulr.paytogether.configuration.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Décodeur JWT dynamique supportant plusieurs realms Keycloak
 */
@Component
public class DynamicRealmJwtDecoder implements JwtDecoder {

    private final Map<String, JwtDecoder> decoders = new ConcurrentHashMap<>();

    @Override
    public Jwt decode(String token) throws JwtException {
        // Décoder le JWT sans vérification pour obtenir l'issuer
        Jwt unverifiedJwt = JwtHelper.decodeUnverified(token);
        String issuer = unverifiedJwt.getIssuer().toString();

        if (issuer == null || issuer.isEmpty()) {
            throw new JwtException("L'issuer est manquant dans le token");
        }

        // Obtenir ou créer le décodeur pour cet issuer
        return getDecoderForIssuer(issuer).decode(token);
    }

    /**
     * Obtient ou crée un décodeur JWT pour un issuer donné
     */
    private JwtDecoder getDecoderForIssuer(String issuer) {
        return decoders.computeIfAbsent(issuer, JwtDecoders::fromIssuerLocation);
    }

    /**
     * Efface le cache des décodeurs
     */
    public void clearCache() {
        decoders.clear();
    }
}
