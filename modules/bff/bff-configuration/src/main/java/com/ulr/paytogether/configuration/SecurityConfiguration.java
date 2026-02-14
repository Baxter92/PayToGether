package com.ulr.paytogether.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configuration de la sécurité
 * Fournit le bean BCryptPasswordEncoder pour le hachage des mots de passe
 */
@Configuration
public class SecurityConfiguration {

    /**
     * Bean BCryptPasswordEncoder pour hacher les mots de passe
     * Utilise le coût par défaut de 10 rounds
     *
     * @return instance de BCryptPasswordEncoder
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

