package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.UtilisateurJpaMapper;
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
 * Tests unitaires pour UtilisateurProviderAdapter
 */
@ExtendWith(MockitoExtension.class)
class UtilisateurProviderAdapterTest {

    @Mock
    private UtilisateurRepository jpaRepository;

    @Mock
    private UtilisateurJpaMapper mapper;

    @InjectMocks
    private UtilisateurProviderAdapter providerAdapter;

    private UtilisateurModele utilisateurModele;
    private UtilisateurJpa utilisateurJpa;
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

        utilisateurJpa = UtilisateurJpa.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .motDePasse("motDePasseHash")
                .statut(com.ulr.paytogether.provider.adapter.entity.enumeration.StatutUtilisateur.ACTIF)
                .role(com.ulr.paytogether.provider.adapter.entity.enumeration.RoleUtilisateur.UTILISATEUR)
                .build();
    }

    @Test
    void testSauvegarder_DevraitSauvegarderUtilisateur() {
        // Given
        when(mapper.versEntite(utilisateurModele)).thenReturn(utilisateurJpa);
        when(jpaRepository.save(utilisateurJpa)).thenReturn(utilisateurJpa);
        when(mapper.versModele(utilisateurJpa)).thenReturn(utilisateurModele);

        // When
        UtilisateurModele resultat = providerAdapter.sauvegarder(utilisateurModele);

        // Then
        assertNotNull(resultat);
        assertEquals("jean.dupont@example.com", resultat.getEmail());
        verify(mapper, times(1)).versEntite(utilisateurModele);
        verify(jpaRepository, times(1)).save(utilisateurJpa);
        verify(mapper, times(1)).versModele(utilisateurJpa);
    }

    @Test
    void testTrouverParUuid_DevraitRetournerUtilisateur() {
        // Given
        when(jpaRepository.findById(uuidUtilisateur)).thenReturn(Optional.of(utilisateurJpa));
        when(mapper.versModele(utilisateurJpa)).thenReturn(utilisateurModele);

        // When
        Optional<UtilisateurModele> resultat = providerAdapter.trouverParUuid(uuidUtilisateur);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("jean.dupont@example.com", resultat.get().getEmail());
        verify(jpaRepository, times(1)).findById(uuidUtilisateur);
        verify(mapper, times(1)).versModele(utilisateurJpa);
    }

    @Test
    void testTrouverParUuid_DevraitRetournerOptionalVide() {
        // Given
        when(jpaRepository.findById(uuidUtilisateur)).thenReturn(Optional.empty());

        // When
        Optional<UtilisateurModele> resultat = providerAdapter.trouverParUuid(uuidUtilisateur);

        // Then
        assertFalse(resultat.isPresent());
        verify(jpaRepository, times(1)).findById(uuidUtilisateur);
        verify(mapper, never()).versModele(any());
    }

    @Test
    void testTrouverParEmail_DevraitRetournerUtilisateur() {
        // Given
        when(jpaRepository.findByEmail("jean.dupont@example.com")).thenReturn(Optional.of(utilisateurJpa));
        when(mapper.versModele(utilisateurJpa)).thenReturn(utilisateurModele);

        // When
        Optional<UtilisateurModele> resultat = providerAdapter.trouverParEmail("jean.dupont@example.com");

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("Dupont", resultat.get().getNom());
        verify(jpaRepository, times(1)).findByEmail("jean.dupont@example.com");
        verify(mapper, times(1)).versModele(utilisateurJpa);
    }

    @Test
    void testTrouverTous_DevraitRetournerListeUtilisateurs() {
        // Given
        UtilisateurJpa utilisateurJpa2 = UtilisateurJpa.builder()
                .uuid(UUID.randomUUID())
                .nom("Martin")
                .email("marie.martin@example.com")
                .build();
        UtilisateurModele utilisateurModele2 = UtilisateurModele.builder()
                .uuid(utilisateurJpa2.getUuid())
                .nom("Martin")
                .email("marie.martin@example.com")
                .build();

        when(jpaRepository.findAll()).thenReturn(Arrays.asList(utilisateurJpa, utilisateurJpa2));
        when(mapper.versModele(utilisateurJpa)).thenReturn(utilisateurModele);
        when(mapper.versModele(utilisateurJpa2)).thenReturn(utilisateurModele2);

        // When
        List<UtilisateurModele> resultat = providerAdapter.trouverTous();

        // Then
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        verify(jpaRepository, times(1)).findAll();
        verify(mapper, times(2)).versModele(any(UtilisateurJpa.class));
    }

    @Test
    void testMettreAJour_DevraitMettreAJourUtilisateur() {
        // Given
        UtilisateurModele utilisateurModifie = UtilisateurModele.builder()
                .nom("Durand")
                .prenom("Jacques")
                .email("jacques.durand@example.com")
                .build();

        when(jpaRepository.findById(uuidUtilisateur)).thenReturn(Optional.of(utilisateurJpa));
        doNothing().when(mapper).mettreAJour(utilisateurJpa, utilisateurModifie);
        when(jpaRepository.save(utilisateurJpa)).thenReturn(utilisateurJpa);
        when(mapper.versModele(utilisateurJpa)).thenReturn(utilisateurModifie);

        // When
        UtilisateurModele resultat = providerAdapter.mettreAJour(uuidUtilisateur, utilisateurModifie);

        // Then
        assertNotNull(resultat);
        verify(jpaRepository, times(1)).findById(uuidUtilisateur);
        verify(mapper, times(1)).mettreAJour(utilisateurJpa, utilisateurModifie);
        verify(jpaRepository, times(1)).save(utilisateurJpa);
        verify(mapper, times(1)).versModele(utilisateurJpa);
    }

    @Test
    void testMettreAJour_DevraitLancerExceptionSiUtilisateurNonTrouve() {
        // Given
        when(jpaRepository.findById(uuidUtilisateur)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            providerAdapter.mettreAJour(uuidUtilisateur, utilisateurModele);
        });
        verify(jpaRepository, times(1)).findById(uuidUtilisateur);
        verify(jpaRepository, never()).save(any());
    }

    @Test
    void testSupprimerParUuid_DevraitSupprimerUtilisateur() {
        // Given
        doNothing().when(jpaRepository).deleteById(uuidUtilisateur);

        // When
        providerAdapter.supprimerParUuid(uuidUtilisateur);

        // Then
        verify(jpaRepository, times(1)).deleteById(uuidUtilisateur);
    }

    @Test
    void testExisteParEmail_DevraitRetournerTrue() {
        // Given
        when(jpaRepository.existsByEmail("jean.dupont@example.com")).thenReturn(true);

        // When
        boolean resultat = providerAdapter.existeParEmail("jean.dupont@example.com");

        // Then
        assertTrue(resultat);
        verify(jpaRepository, times(1)).existsByEmail("jean.dupont@example.com");
    }

    @Test
    void testExisteParEmail_DevraitRetournerFalse() {
        // Given
        when(jpaRepository.existsByEmail("inexistant@example.com")).thenReturn(false);

        // When
        boolean resultat = providerAdapter.existeParEmail("inexistant@example.com");

        // Then
        assertFalse(resultat);
        verify(jpaRepository, times(1)).existsByEmail("inexistant@example.com");
    }
}
