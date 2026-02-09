package com.ulr.paytogether.wsclient.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Service de gestion des tokens JWT
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret:monSecretSuperSecurisePourPayTogether2024!}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 heures par défaut
    private Long expiration;

    /**
     * Génère la clé de signature
     */
    private Key getCleSignature() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Génère un token JWT pour un utilisateur
     * @param uuidUtilisateur l'UUID de l'utilisateur
     * @param email l'email de l'utilisateur
     * @param role le rôle de l'utilisateur
     * @return le token JWT
     */
    public String genererToken(UUID uuidUtilisateur, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uuid", uuidUtilisateur.toString());
        claims.put("email", email);
        claims.put("role", role);

        return creerToken(claims, email);
    }

    /**
     * Crée un token JWT
     */
    private String creerToken(Map<String, Object> claims, String sujet) {
        Date maintenant = new Date();
        Date dateExpiration = new Date(maintenant.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(sujet)
                .setIssuedAt(maintenant)
                .setExpiration(dateExpiration)
                .signWith(getCleSignature(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrait toutes les claims d'un token
     */
    private Claims extraireClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getCleSignature())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extrait une claim spécifique d'un token
     */
    public <T> T extraireClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraireClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrait l'email du token
     */
    public String extraireEmail(String token) {
        return extraireClaim(token, Claims::getSubject);
    }

    /**
     * Extrait l'UUID de l'utilisateur du token
     */
    public UUID extraireUuidUtilisateur(String token) {
        String uuid = extraireClaim(token, claims -> claims.get("uuid", String.class));
        return UUID.fromString(uuid);
    }

    /**
     * Extrait le rôle de l'utilisateur du token
     */
    public String extraireRole(String token) {
        return extraireClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extrait la date d'expiration du token
     */
    public Date extraireDateExpiration(String token) {
        return extraireClaim(token, Claims::getExpiration);
    }

    /**
     * Vérifie si le token est expiré
     */
    public Boolean estTokenExpire(String token) {
        try {
            return extraireDateExpiration(token).before(new Date());
        } catch (Exception e) {
            log.error("Erreur lors de la vérification de l'expiration du token", e);
            return true;
        }
    }

    /**
     * Valide un token JWT
     */
    public Boolean validerToken(String token, String email) {
        try {
            final String emailToken = extraireEmail(token);
            return (emailToken.equals(email) && !estTokenExpire(token));
        } catch (Exception e) {
            log.error("Erreur lors de la validation du token", e);
            return false;
        }
    }
}
