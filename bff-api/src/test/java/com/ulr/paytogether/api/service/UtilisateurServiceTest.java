package com.ulr.paytogether.api.service;

import com.ulr.paytogether.core.domaine.entite.Utilisateur;
import com.ulr.paytogether.provider.adapter.entity.enumeration.RoleUtilisateur;
import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutUtilisateur;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UtilisateurService
 */
@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private UtilisateurService utilisateurService;

    private Utilisateur utilisateur;
    private UUID uuidUtilisateur;

    @BeforeEach
    void setUp() {
        uuidUtilisateur = UUID.randomUUID();
        utilisateur = Utilisateur.builder()
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .motDePasse("motDePasseHash")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.UTILISATEUR)
                .build();
    }

    @Test
    void testCreer_DevraitCreerUtilisateur() {
        // Given
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // When
        Utilisateur resultat = utilisateurService.creer(utilisateur);

        // Then
        assertNotNull(resultat);
        assertEquals("jean.dupont@example.com", resultat.getEmail());
        verify(utilisateurRepository, times(1)).save(utilisateur);
    }

    @Test
    void testLireParUuid_DevraitRetournerUtilisateur() {
        // Given
        when(utilisateurRepository.findById(uuidUtilisateur)).thenReturn(Optional.of(utilisateur));

        // When
        Optional<Utilisateur> resultat = utilisateurService.lireParUuid(uuidUtilisateur);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("jean.dupont@example.com", resultat.get().getEmail());
        verify(utilisateurRepository, times(1)).findById(uuidUtilisateur);
    }

    @Test
    void testLireParEmail_DevraitRetournerUtilisateur() {
        // Given
        when(utilisateurRepository.findByEmail("jean.dupont@example.com")).thenReturn(Optional.of(utilisateur));

        // When
        Optional<Utilisateur> resultat = utilisateurService.lireParEmail("jean.dupont@example.com");

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("Dupont", resultat.get().getNom());
        verify(utilisateurRepository, times(1)).findByEmail("jean.dupont@example.com");
    }

    @Test
    void testLireTous_DevraitRetournerListeUtilisateurs() {
        // Given
        Utilisateur utilisateur2 = Utilisateur.builder()
                .nom("Martin")
                .prenom("Marie")
                .email("marie.martin@example.com")
                .motDePasse("motDePasseHash")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.UTILISATEUR)
                .build();
        when(utilisateurRepository.findAll()).thenReturn(Arrays.asList(utilisateur, utilisateur2));

        // When
        List<Utilisateur> resultat = utilisateurService.lireTous();

        // Then
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        verify(utilisateurRepository, times(1)).findAll();
    }

    @Test
    void testMettreAJour_DevraitMettreAJourUtilisateur() {
        // Given
        Utilisateur utilisateurModifie = Utilisateur.builder()
                .nom("Durand")
                .prenom("Jacques")
                .email("jacques.durand@example.com")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.VENDEUR)
                .photoProfil("http://example.com/photo.jpg")
                .build();

        when(utilisateurRepository.findById(uuidUtilisateur)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // When
        Utilisateur resultat = utilisateurService.mettreAJour(uuidUtilisateur, utilisateurModifie);

        // Then
        assertNotNull(resultat);
        verify(utilisateurRepository, times(1)).findById(uuidUtilisateur);
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    void testMettreAJour_DevraitLancerExceptionSiUtilisateurNonTrouve() {
        // Given
        when(utilisateurRepository.findById(uuidUtilisateur)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            utilisateurService.mettreAJour(uuidUtilisateur, utilisateur);
        });
        verify(utilisateurRepository, times(1)).findById(uuidUtilisateur);
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void testSupprimer_DevraitSupprimerUtilisateur() {
        // When
        utilisateurService.supprimer(uuidUtilisateur);

        // Then
        verify(utilisateurRepository, times(1)).deleteById(uuidUtilisateur);
    }

    @Test
    void testExisteParEmail_DevraitRetournerTrue() {
        // Given
        when(utilisateurRepository.existsByEmail("jean.dupont@example.com")).thenReturn(true);

        // When
        boolean resultat = utilisateurService.existeParEmail("jean.dupont@example.com");

        // Then
        assertTrue(resultat);
        verify(utilisateurRepository, times(1)).existsByEmail("jean.dupont@example.com");
    }

    @Test
    void testExisteParEmail_DevraitRetournerFalse() {
        // Given
        when(utilisateurRepository.existsByEmail("inexistant@example.com")).thenReturn(false);

        // When
        boolean resultat = utilisateurService.existeParEmail("inexistant@example.com");

        // Then
        assertFalse(resultat);
        verify(utilisateurRepository, times(1)).existsByEmail("inexistant@example.com");
    }
}
