package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.CreerUtilisateurDTO;
import com.ulr.paytogether.api.dto.UtilisateurDTO;
import com.ulr.paytogether.api.mapper.UtilisateurMapper;
import com.ulr.paytogether.core.domaine.service.UtilisateurService;
import com.ulr.paytogether.core.enumeration.RoleUtilisateur;
import com.ulr.paytogether.core.enumeration.StatutUtilisateur;
import com.ulr.paytogether.core.modele.UtilisateurModele;
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
 * Tests unitaires pour UtilisateurApiAdapter
 */
@ExtendWith(MockitoExtension.class)
class UtilisateurApiAdapterTest {

    @Mock
    private UtilisateurService utilisateurService;

    @Mock
    private UtilisateurMapper mapper;

    @InjectMocks
    private UtilisateurApiAdapter apiAdapter;

    private UtilisateurModele utilisateurModele;
    private UtilisateurDTO utilisateurDTO;
    private CreerUtilisateurDTO creerUtilisateurDTO;
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

        utilisateurDTO = UtilisateurDTO.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.UTILISATEUR)
                .build();

        creerUtilisateurDTO = CreerUtilisateurDTO.builder()
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .motDePasse("motDePasse123")
                .build();
    }

    @Test
    void testCreer_DevraitCreerUtilisateur() {
        // Given
        when(mapper.dtoVersModele(creerUtilisateurDTO)).thenReturn(utilisateurModele);
        when(utilisateurService.creer(utilisateurModele)).thenReturn(utilisateurModele);
        when(mapper.modeleVersDto(utilisateurModele)).thenReturn(utilisateurDTO);

        // When
        UtilisateurDTO resultat = apiAdapter.creer(creerUtilisateurDTO);

        // Then
        assertNotNull(resultat);
        assertEquals("jean.dupont@example.com", resultat.getEmail());
        assertEquals("Dupont", resultat.getNom());
        verify(mapper, times(1)).dtoVersModele(creerUtilisateurDTO);
        verify(utilisateurService, times(1)).creer(utilisateurModele);
        verify(mapper, times(1)).modeleVersDto(utilisateurModele);
    }

    @Test
    void testTrouverParUuid_DevraitRetournerUtilisateur() {
        // Given
        when(utilisateurService.lireParUuid(uuidUtilisateur)).thenReturn(Optional.of(utilisateurModele));
        when(mapper.modeleVersDto(utilisateurModele)).thenReturn(utilisateurDTO);

        // When
        Optional<UtilisateurDTO> resultat = apiAdapter.trouverParUuid(uuidUtilisateur);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("jean.dupont@example.com", resultat.get().getEmail());
        verify(utilisateurService, times(1)).lireParUuid(uuidUtilisateur);
        verify(mapper, times(1)).modeleVersDto(utilisateurModele);
    }

    @Test
    void testTrouverParUuid_DevraitRetournerOptionalVide() {
        // Given
        when(utilisateurService.lireParUuid(uuidUtilisateur)).thenReturn(Optional.empty());

        // When
        Optional<UtilisateurDTO> resultat = apiAdapter.trouverParUuid(uuidUtilisateur);

        // Then
        assertFalse(resultat.isPresent());
        verify(utilisateurService, times(1)).lireParUuid(uuidUtilisateur);
        verify(mapper, never()).modeleVersDto(any());
    }

    @Test
    void testTrouverParEmail_DevraitRetournerUtilisateur() {
        // Given
        when(utilisateurService.lireParEmail("jean.dupont@example.com")).thenReturn(Optional.of(utilisateurModele));
        when(mapper.modeleVersDto(utilisateurModele)).thenReturn(utilisateurDTO);

        // When
        Optional<UtilisateurDTO> resultat = apiAdapter.trouverParEmail("jean.dupont@example.com");

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("Dupont", resultat.get().getNom());
        verify(utilisateurService, times(1)).lireParEmail("jean.dupont@example.com");
        verify(mapper, times(1)).modeleVersDto(utilisateurModele);
    }

    @Test
    void testTrouverTous_DevraitRetournerListeUtilisateurs() {
        // Given
        UtilisateurModele utilisateurModele2 = UtilisateurModele.builder()
                .uuid(UUID.randomUUID())
                .nom("Martin")
                .email("marie.martin@example.com")
                .build();
        UtilisateurDTO utilisateurDTO2 = UtilisateurDTO.builder()
                .uuid(utilisateurModele2.getUuid())
                .nom("Martin")
                .email("marie.martin@example.com")
                .build();

        when(utilisateurService.lireTous()).thenReturn(Arrays.asList(utilisateurModele, utilisateurModele2));
        when(mapper.modeleVersDto(utilisateurModele)).thenReturn(utilisateurDTO);
        when(mapper.modeleVersDto(utilisateurModele2)).thenReturn(utilisateurDTO2);

        // When
        List<UtilisateurDTO> resultat = apiAdapter.trouverTous();

        // Then
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        verify(utilisateurService, times(1)).lireTous();
        verify(mapper, times(2)).modeleVersDto(any(UtilisateurModele.class));
    }

    @Test
    void testMettreAJour_DevraitMettreAJourUtilisateur() {
        // Given
        UtilisateurDTO utilisateurModifieDTO = UtilisateurDTO.builder()
                .uuid(uuidUtilisateur)
                .nom("Durand")
                .prenom("Jacques")
                .email("jacques.durand@example.com")
                .build();
        UtilisateurModele utilisateurModifieModele = UtilisateurModele.builder()
                .uuid(uuidUtilisateur)
                .nom("Durand")
                .prenom("Jacques")
                .email("jacques.durand@example.com")
                .build();

        when(mapper.dtoVersModele(utilisateurModifieDTO)).thenReturn(utilisateurModifieModele);
        when(utilisateurService.mettreAJour(eq(uuidUtilisateur), any(UtilisateurModele.class)))
                .thenReturn(utilisateurModifieModele);
        when(mapper.modeleVersDto(utilisateurModifieModele)).thenReturn(utilisateurModifieDTO);

        // When
        UtilisateurDTO resultat = apiAdapter.mettreAJour(uuidUtilisateur, utilisateurModifieDTO);

        // Then
        assertNotNull(resultat);
        assertEquals("Durand", resultat.getNom());
        verify(mapper, times(1)).dtoVersModele(utilisateurModifieDTO);
        verify(utilisateurService, times(1)).mettreAJour(eq(uuidUtilisateur), any(UtilisateurModele.class));
        verify(mapper, times(1)).modeleVersDto(utilisateurModifieModele);
    }

    @Test
    void testSupprimer_DevraitSupprimerUtilisateur() {
        // Given
        doNothing().when(utilisateurService).supprimerParUuid(uuidUtilisateur);

        // When
        apiAdapter.supprimer(uuidUtilisateur);

        // Then
        verify(utilisateurService, times(1)).supprimerParUuid(uuidUtilisateur);
    }

    @Test
    void testExisteParEmail_DevraitRetournerTrue() {
        // Given
        when(utilisateurService.existeParEmail("jean.dupont@example.com")).thenReturn(true);

        // When
        boolean resultat = apiAdapter.existeParEmail("jean.dupont@example.com");

        // Then
        assertTrue(resultat);
        verify(utilisateurService, times(1)).existeParEmail("jean.dupont@example.com");
    }

    @Test
    void testExisteParEmail_DevraitRetournerFalse() {
        // Given
        when(utilisateurService.existeParEmail("inexistant@example.com")).thenReturn(false);

        // When
        boolean resultat = apiAdapter.existeParEmail("inexistant@example.com");

        // Then
        assertFalse(resultat);
        verify(utilisateurService, times(1)).existeParEmail("inexistant@example.com");
    }
}
