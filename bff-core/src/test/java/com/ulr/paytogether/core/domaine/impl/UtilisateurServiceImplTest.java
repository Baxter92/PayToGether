package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.core.provider.UtilisateurProvider;
import com.ulr.paytogether.provider.adapter.entity.enumeration.RoleUtilisateur;
import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutUtilisateur;
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
 * Tests unitaires pour UtilisateurServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class UtilisateurServiceImplTest {

    @Mock
    private UtilisateurProvider utilisateurProvider;

    @InjectMocks
    private UtilisationServiceImpl utilisateurService;

    private UtilisateurModele utilisateurModele;
    private UUID uuidUtilisateur;

    @BeforeEach
    void setUp() {
        uuidUtilisateur = UUID.randomUUID();
        utilisateurModele = UtilisateurModele.builder()
                .uuid(uuidUtilisateur)
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
        when(utilisateurProvider.sauvegarder(any(UtilisateurModele.class))).thenReturn(utilisateurModele);

        // When
        UtilisateurModele resultat = utilisateurService.creer(utilisateurModele);

        // Then
        assertNotNull(resultat);
        assertEquals("jean.dupont@example.com", resultat.getEmail());
        assertEquals("Dupont", resultat.getNom());
        verify(utilisateurProvider, times(1)).sauvegarder(utilisateurModele);
    }

    @Test
    void testLireParUuid_DevraitRetournerUtilisateur() {
        // Given
        when(utilisateurProvider.trouverParUuid(uuidUtilisateur)).thenReturn(Optional.of(utilisateurModele));

        // When
        Optional<UtilisateurModele> resultat = utilisateurService.lireParUuid(uuidUtilisateur);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("jean.dupont@example.com", resultat.get().getEmail());
        assertEquals(uuidUtilisateur, resultat.get().getUuid());
        verify(utilisateurProvider, times(1)).trouverParUuid(uuidUtilisateur);
    }

    @Test
    void testLireParUuid_DevraitRetournerOptionalVide() {
        // Given
        when(utilisateurProvider.trouverParUuid(uuidUtilisateur)).thenReturn(Optional.empty());

        // When
        Optional<UtilisateurModele> resultat = utilisateurService.lireParUuid(uuidUtilisateur);

        // Then
        assertFalse(resultat.isPresent());
        verify(utilisateurProvider, times(1)).trouverParUuid(uuidUtilisateur);
    }

    @Test
    void testLireParEmail_DevraitRetournerUtilisateur() {
        // Given
        when(utilisateurProvider.trouverParEmail("jean.dupont@example.com")).thenReturn(Optional.of(utilisateurModele));

        // When
        Optional<UtilisateurModele> resultat = utilisateurService.lireParEmail("jean.dupont@example.com");

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("Dupont", resultat.get().getNom());
        verify(utilisateurProvider, times(1)).trouverParEmail("jean.dupont@example.com");
    }

    @Test
    void testLireTous_DevraitRetournerListeUtilisateurs() {
        // Given
        UtilisateurModele utilisateurModele2 = UtilisateurModele.builder()
                .uuid(UUID.randomUUID())
                .nom("Martin")
                .prenom("Marie")
                .email("marie.martin@example.com")
                .motDePasse("motDePasseHash")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.UTILISATEUR)
                .build();
        when(utilisateurProvider.trouverTous()).thenReturn(Arrays.asList(utilisateurModele, utilisateurModele2));

        // When
        List<UtilisateurModele> resultat = utilisateurService.lireTous();

        // Then
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        verify(utilisateurProvider, times(1)).trouverTous();
    }

    @Test
    void testMettreAJour_DevraitMettreAJourUtilisateur() {
        // Given
        UtilisateurModele utilisateurModifie = UtilisateurModele.builder()
                .uuid(uuidUtilisateur)
                .nom("Durand")
                .prenom("Jacques")
                .email("jacques.durand@example.com")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.VENDEUR)
                .photoProfil("http://example.com/photo.jpg")
                .build();

        when(utilisateurProvider.mettreAJour(eq(uuidUtilisateur), any(UtilisateurModele.class)))
                .thenReturn(utilisateurModifie);

        // When
        UtilisateurModele resultat = utilisateurService.mettreAJour(uuidUtilisateur, utilisateurModifie);

        // Then
        assertNotNull(resultat);
        assertEquals("Durand", resultat.getNom());
        verify(utilisateurProvider, times(1)).mettreAJour(eq(uuidUtilisateur), any(UtilisateurModele.class));
    }

    @Test
    void testSupprimerParUuid_DevraitSupprimerUtilisateur() {
        // Given
        doNothing().when(utilisateurProvider).supprimerParUuid(uuidUtilisateur);

        // When
        utilisateurService.supprimerParUuid(uuidUtilisateur);

        // Then
        verify(utilisateurProvider, times(1)).supprimerParUuid(uuidUtilisateur);
    }

    @Test
    void testExisteParEmail_DevraitRetournerTrue() {
        // Given
        when(utilisateurProvider.existeParEmail("jean.dupont@example.com")).thenReturn(true);

        // When
        boolean resultat = utilisateurService.existeParEmail("jean.dupont@example.com");

        // Then
        assertTrue(resultat);
        verify(utilisateurProvider, times(1)).existeParEmail("jean.dupont@example.com");
    }

    @Test
    void testExisteParEmail_DevraitRetournerFalse() {
        // Given
        when(utilisateurProvider.existeParEmail("inexistant@example.com")).thenReturn(false);

        // When
        boolean resultat = utilisateurService.existeParEmail("inexistant@example.com");

        // Then
        assertFalse(resultat);
        verify(utilisateurProvider, times(1)).existeParEmail("inexistant@example.com");
    }
}
