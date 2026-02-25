package com.ulr.paytogether.api.service;

import com.ulr.paytogether.core.domaine.impl.UtilisateurServiceImpl;
import com.ulr.paytogether.core.domaine.validator.UtilisateurValidator;
import com.ulr.paytogether.core.enumeration.RoleUtilisateur;
import com.ulr.paytogether.core.enumeration.StatutUtilisateur;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.core.provider.UtilisateurProvider;
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
class UtilisateurServiceTest {

    @Mock
    private UtilisateurProvider utilisateurProvider;

    @Mock
    private UtilisateurValidator utilisateurValidator;

    @InjectMocks
    private UtilisateurServiceImpl utilisateurService;

    private UtilisateurModele utilisateur;
    private UUID uuidUtilisateur;

    @BeforeEach
    void setUp() {
        uuidUtilisateur = UUID.randomUUID();
        utilisateur = UtilisateurModele.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .motDePasse("motDePasseSecurise123")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.UTILISATEUR)
                .build();
    }

    // ==================== Tests pour creer() ====================

    @Test
    void testCreer_DevraitCreerUtilisateur() {
        // Given
        doNothing().when(utilisateurValidator).validerPourCreation(any(UtilisateurModele.class));
        when(utilisateurProvider.sauvegarder(any(UtilisateurModele.class))).thenReturn(utilisateur);

        // When
        UtilisateurModele resultat = utilisateurService.creer(utilisateur);

        // Then
        assertNotNull(resultat);
        assertEquals("jean.dupont@example.com", resultat.getEmail());
        assertEquals("Dupont", resultat.getNom());
        assertEquals("Jean", resultat.getPrenom());
        verify(utilisateurValidator, times(1)).validerPourCreation(utilisateur);
        verify(utilisateurProvider, times(1)).sauvegarder(utilisateur);
    }

    @Test
    void testCreer_DevraitLancerExceptionSiValidationEchoue() {
        // Given
        doThrow(new IllegalArgumentException("L'attribut email est obligatoire"))
                .when(utilisateurValidator).validerPourCreation(any(UtilisateurModele.class));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> utilisateurService.creer(utilisateur)
        );

        assertEquals("L'attribut email est obligatoire", exception.getMessage());
        verify(utilisateurValidator, times(1)).validerPourCreation(utilisateur);
        verify(utilisateurProvider, never()).sauvegarder(any());
    }

    // ==================== Tests pour lireParUuid() ====================

    @Test
    void testLireParUuid_DevraitRetournerUtilisateur() {
        // Given
        when(utilisateurProvider.trouverParUuid(uuidUtilisateur)).thenReturn(Optional.of(utilisateur));

        // When
        Optional<UtilisateurModele> resultat = utilisateurService.lireParUuid(uuidUtilisateur);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals(uuidUtilisateur, resultat.get().getUuid());
        assertEquals("jean.dupont@example.com", resultat.get().getEmail());
        verify(utilisateurProvider, times(1)).trouverParUuid(uuidUtilisateur);
    }

    @Test
    void testLireParUuid_DevraitRetournerOptionalVide() {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        when(utilisateurProvider.trouverParUuid(uuidInexistant)).thenReturn(Optional.empty());

        // When
        Optional<UtilisateurModele> resultat = utilisateurService.lireParUuid(uuidInexistant);

        // Then
        assertFalse(resultat.isPresent());
        verify(utilisateurProvider, times(1)).trouverParUuid(uuidInexistant);
    }

    // ==================== Tests pour lireParEmail() ====================

    @Test
    void testLireParEmail_DevraitRetournerUtilisateur() {
        // Given
        String email = "jean.dupont@example.com";
        when(utilisateurProvider.trouverParEmail(email)).thenReturn(Optional.of(utilisateur));

        // When
        Optional<UtilisateurModele> resultat = utilisateurService.lireParEmail(email);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals(email, resultat.get().getEmail());
        verify(utilisateurProvider, times(1)).trouverParEmail(email);
    }

    @Test
    void testLireParEmail_DevraitRetournerOptionalVide() {
        // Given
        String emailInexistant = "inexistant@example.com";
        when(utilisateurProvider.trouverParEmail(emailInexistant)).thenReturn(Optional.empty());

        // When
        Optional<UtilisateurModele> resultat = utilisateurService.lireParEmail(emailInexistant);

        // Then
        assertFalse(resultat.isPresent());
        verify(utilisateurProvider, times(1)).trouverParEmail(emailInexistant);
    }

    // ==================== Tests pour lireTous() ====================

    @Test
    void testLireTous_DevraitRetournerTousLesUtilisateurs() {
        // Given
        UtilisateurModele utilisateur2 = UtilisateurModele.builder()
                .uuid(UUID.randomUUID())
                .nom("Martin")
                .prenom("Marie")
                .email("marie.martin@example.com")
                .motDePasse("motDePasseSecurise456")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.VENDEUR)
                .build();

        List<UtilisateurModele> utilisateurs = Arrays.asList(utilisateur, utilisateur2);
        when(utilisateurProvider.trouverTous()).thenReturn(utilisateurs);

        // When
        List<UtilisateurModele> resultat = utilisateurService.lireTous();

        // Then
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        assertEquals("jean.dupont@example.com", resultat.get(0).getEmail());
        assertEquals("marie.martin@example.com", resultat.get(1).getEmail());
        verify(utilisateurProvider, times(1)).trouverTous();
    }

    @Test
    void testLireTous_DevraitRetournerListeVide() {
        // Given
        when(utilisateurProvider.trouverTous()).thenReturn(List.of());

        // When
        List<UtilisateurModele> resultat = utilisateurService.lireTous();

        // Then
        assertNotNull(resultat);
        assertTrue(resultat.isEmpty());
        verify(utilisateurProvider, times(1)).trouverTous();
    }

    // ==================== Tests pour mettreAJour() ====================

    @Test
    void testMettreAJour_DevraitMettreAJourUtilisateur() {
        // Given
        UtilisateurModele utilisateurMisAJour = UtilisateurModele.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean-Claude")
                .email("jean.dupont@example.com")
                .motDePasse("nouveauMotDePasse123")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.UTILISATEUR)
                .build();

        doNothing().when(utilisateurValidator).validerPourMiseAJour(any(UtilisateurModele.class), any());
        when(utilisateurProvider.mettreAJour(uuidUtilisateur, utilisateurMisAJour, anyString())).thenReturn(utilisateurMisAJour);

        // When
        UtilisateurModele resultat = utilisateurService.mettreAJour(uuidUtilisateur, utilisateurMisAJour, "tokenDeTest");

        // Then
        assertNotNull(resultat);
        assertEquals("Jean-Claude", resultat.getPrenom());
        verify(utilisateurValidator, times(1)).validerPourMiseAJour(utilisateurMisAJour, UUID.randomUUID());
        verify(utilisateurProvider, times(1)).mettreAJour(uuidUtilisateur, utilisateurMisAJour, anyString());
    }

    @Test
    void testMettreAJour_DevraitLancerExceptionSiValidationEchoue() {
        // Given
        doThrow(new IllegalArgumentException("L'attribut uuid est obligatoire"))
                .when(utilisateurValidator).validerPourMiseAJour(any(UtilisateurModele.class), null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> utilisateurService.mettreAJour(uuidUtilisateur, utilisateur, "tokenDeTest")
        );

        assertEquals("L'attribut uuid est obligatoire", exception.getMessage());
        verify(utilisateurValidator, times(1)).validerPourMiseAJour(utilisateur, null);
        verify(utilisateurProvider, never()).mettreAJour(any(), any(), anyString());
    }

    // ==================== Tests pour supprimerParUuid() ====================

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
    void testSupprimerParUuid_AvecUuidNull() {
        // Given
        doThrow(new IllegalArgumentException("L'UUID ne peut pas être null"))
                .when(utilisateurProvider).supprimerParUuid(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> utilisateurService.supprimerParUuid(null));
        verify(utilisateurProvider, times(1)).supprimerParUuid(null);
    }

    // ==================== Tests pour existeParEmail() ====================

    @Test
    void testExisteParEmail_DevraitRetournerTrueQuandEmailExiste() {
        // Given
        String email = "jean.dupont@example.com";
        when(utilisateurProvider.existeParEmail(email)).thenReturn(true);

        // When
        boolean resultat = utilisateurService.existeParEmail(email);

        // Then
        assertTrue(resultat);
        verify(utilisateurProvider, times(1)).existeParEmail(email);
    }

    @Test
    void testExisteParEmail_DevraitRetournerFalseQuandEmailNExistePas() {
        // Given
        String email = "inexistant@example.com";
        when(utilisateurProvider.existeParEmail(email)).thenReturn(false);

        // When
        boolean resultat = utilisateurService.existeParEmail(email);

        // Then
        assertFalse(resultat);
        verify(utilisateurProvider, times(1)).existeParEmail(email);
    }

    // ==================== Tests avec différents rôles ====================

    @Test
    void testCreer_AvecRoleMarchand() {
        // Given
        UtilisateurModele marchand = UtilisateurModele.builder()
                .nom("Commerçant")
                .prenom("Pierre")
                .email("pierre@commerce.com")
                .motDePasse("motDePasseSecurise789")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.VENDEUR)
                .build();

        doNothing().when(utilisateurValidator).validerPourCreation(marchand);
        when(utilisateurProvider.sauvegarder(marchand)).thenReturn(marchand);

        // When
        UtilisateurModele resultat = utilisateurService.creer(marchand);

        // Then
        assertNotNull(resultat);
        assertEquals(RoleUtilisateur.VENDEUR, resultat.getRole());
        verify(utilisateurValidator, times(1)).validerPourCreation(marchand);
        verify(utilisateurProvider, times(1)).sauvegarder(marchand);
    }

    @Test
    void testCreer_AvecRoleAdmin() {
        // Given
        UtilisateurModele admin = UtilisateurModele.builder()
                .nom("Admin")
                .prenom("Super")
                .email("admin@paytogether.com")
                .motDePasse("motDePasseSuperSecurise999")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.ADMIN)
                .build();

        doNothing().when(utilisateurValidator).validerPourCreation(admin);
        when(utilisateurProvider.sauvegarder(admin)).thenReturn(admin);

        // When
        UtilisateurModele resultat = utilisateurService.creer(admin);

        // Then
        assertNotNull(resultat);
        assertEquals(RoleUtilisateur.ADMIN, resultat.getRole());
        verify(utilisateurValidator, times(1)).validerPourCreation(admin);
        verify(utilisateurProvider, times(1)).sauvegarder(admin);
    }
}

