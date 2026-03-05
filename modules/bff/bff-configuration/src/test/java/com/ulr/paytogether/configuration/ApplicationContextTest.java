package com.ulr.paytogether.configuration;

import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de chargement du contexte Spring
 * Vérifie que tous les beans se chargent correctement
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationContextTest {

    @Autowired(required = false)
    private UtilisateurRepository utilisateurRepository;

    @Test
    void contextLoads() {
        // Le test passe si le contexte Spring se charge correctement
        assertThat(utilisateurRepository).isNotNull();
    }

    @Test
    void utilisateurRepositoryBeanExists() {
        assertThat(utilisateurRepository).isNotNull();
    }
}

