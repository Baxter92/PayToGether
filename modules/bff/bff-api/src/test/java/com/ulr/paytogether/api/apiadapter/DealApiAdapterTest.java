package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.DealDTO;
import com.ulr.paytogether.api.dto.DealResponseDto;
import com.ulr.paytogether.api.mapper.DealMapper;
import com.ulr.paytogether.core.domaine.service.DealService;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour DealApiAdapter
 */
@ExtendWith(MockitoExtension.class)
class DealApiAdapterTest {

    @Mock
    private DealService dealService;

    @Mock
    private DealMapper mapper;

    @InjectMocks
    private DealApiAdapter apiAdapter;

    private DealModele dealModele;
    private DealDTO dealDTO;
    private DealResponseDto dealResponseDto;
    private UUID uuidDeal;
    private UUID uuidCreateur;
    private UUID uuidCategorie;

    @BeforeEach
    void setUp() {
        uuidDeal = UUID.randomUUID();
        uuidCreateur = UUID.randomUUID();
        uuidCategorie = UUID.randomUUID();

        UtilisateurModele createur = UtilisateurModele.builder()
                .uuid(uuidCreateur)
                .nom("Dupont")
                .prenom("Jean")
                .build();

        CategorieModele categorie = CategorieModele.builder()
                .uuid(uuidCategorie)
                .nom("Viandes")
                .build();

        dealModele = DealModele.builder()
                .uuid(uuidDeal)
                .titre("Filet de boeuf premium")
                .description("Viande de qualité supérieure")
                .prixDeal(new BigDecimal("150.00"))
                .prixPart(new BigDecimal("30.00"))
                .nbParticipants(5)
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(7))
                .statut(StatutDeal.PUBLIE)
                .createur(createur)
                .categorie(categorie)
                .ville("Montreal")
                .build();

        dealDTO = DealDTO.builder()
                .uuid(uuidDeal)
                .titre("Filet de boeuf premium")
                .description("Viande de qualité supérieure")
                .prixDeal(new BigDecimal("150.00"))
                .prixPart(new BigDecimal("30.00"))
                .nbParticipants(5)
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(7))
                .statut(StatutDeal.PUBLIE)
                .createurUuid(uuidCreateur)
                .categorieUuid(uuidCategorie)
                .ville("Montreal")
                .build();

        dealResponseDto = DealResponseDto.builder()
                .uuid(uuidDeal)
                .titre("Filet de boeuf premium")
                .description("Viande de qualité supérieure")
                .prixDeal(new BigDecimal("150.00"))
                .prixPart(new BigDecimal("30.00"))
                .nbParticipants(5)
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(7))
                .statut(StatutDeal.PUBLIE)
                .createurNom("Dupont Jean")
                .categorieNom("Viandes")
                .ville("Montreal")
                .build();
    }

    @Test
    void testCreerDeal_DevraitCreerDeal() {
        // Given
        when(mapper.versEntite(dealDTO)).thenReturn(dealModele);
        when(dealService.creer(dealModele)).thenReturn(dealModele);
        when(mapper.versDTO(dealModele)).thenReturn(dealResponseDto);

        // When
        DealResponseDto resultat = apiAdapter.creerDeal(dealDTO);

        // Then
        assertNotNull(resultat);
        assertEquals("Filet de boeuf premium", resultat.getTitre());
        assertEquals(new BigDecimal("150.00"), resultat.getPrixDeal());
        assertEquals("Dupont Jean", resultat.getCreateurNom());
        assertEquals("Viandes", resultat.getCategorieNom());
        verify(mapper, times(1)).versEntite(dealDTO);
        verify(dealService, times(1)).creer(dealModele);
        verify(mapper, times(1)).versDTO(dealModele);
    }

    @Test
    void testLireParUuid_DevraitRetournerDeal() {
        // Given
        when(dealService.lireParUuid(uuidDeal)).thenReturn(Optional.of(dealModele));
        when(mapper.versDTO(dealModele)).thenReturn(dealResponseDto);

        // When
        Optional<DealResponseDto> resultat = apiAdapter.lireParUuid(uuidDeal);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("Filet de boeuf premium", resultat.get().getTitre());
        assertEquals(uuidDeal, resultat.get().getUuid());
        verify(dealService, times(1)).lireParUuid(uuidDeal);
        verify(mapper, times(1)).versDTO(dealModele);
    }

    @Test
    void testLireParUuid_DevraitRetournerOptionalVide() {
        // Given
        when(dealService.lireParUuid(uuidDeal)).thenReturn(Optional.empty());

        // When
        Optional<DealResponseDto> resultat = apiAdapter.lireParUuid(uuidDeal);

        // Then
        assertFalse(resultat.isPresent());
        verify(dealService, times(1)).lireParUuid(uuidDeal);
        verify(mapper, never()).versDTO(any());
    }

    @Test
    void testLireTous_DevraitRetournerListeDeals() {
        // Given
        DealModele dealModele2 = DealModele.builder()
                .uuid(UUID.randomUUID())
                .titre("Saumon frais")
                .description("Poisson de qualité")
                .prixDeal(new BigDecimal("80.00"))
                .prixPart(new BigDecimal("20.00"))
                .nbParticipants(4)
                .statut(StatutDeal.PUBLIE)
                .build();
        DealResponseDto dealResponseDto2 = DealResponseDto.builder()
                .uuid(dealModele2.getUuid())
                .titre("Saumon frais")
                .description("Poisson de qualité")
                .prixDeal(new BigDecimal("80.00"))
                .prixPart(new BigDecimal("20.00"))
                .nbParticipants(4)
                .statut(StatutDeal.PUBLIE)
                .build();

        when(dealService.lireTous()).thenReturn(Arrays.asList(dealModele, dealModele2));
        when(mapper.versDTO(dealModele)).thenReturn(dealResponseDto);
        when(mapper.versDTO(dealModele2)).thenReturn(dealResponseDto2);

        // When
        List<DealResponseDto> resultat = apiAdapter.lireTous();

        // Then
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        assertEquals("Filet de boeuf premium", resultat.get(0).getTitre());
        assertEquals("Saumon frais", resultat.get(1).getTitre());
        verify(dealService, times(1)).lireTous();
        verify(mapper, times(2)).versDTO(any(DealModele.class));
    }

    @Test
    void testLireTousByStatut_DevraitRetournerDealsParStatut() {
        // Given
        when(dealService.lireParStatut(StatutDeal.PUBLIE)).thenReturn(List.of(dealModele));
        when(mapper.versDTO(dealModele)).thenReturn(dealResponseDto);

        // When
        List<DealResponseDto> resultat = apiAdapter.lireTousByStatut(StatutDeal.PUBLIE);

        // Then
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertEquals(StatutDeal.PUBLIE, resultat.get(0).getStatut());
        verify(dealService, times(1)).lireParStatut(StatutDeal.PUBLIE);
        verify(mapper, times(1)).versDTO(dealModele);
    }

    @Test
    void testLireTousByCreateurUuid_DevraitRetournerDealsCreateur() {
        // Given
        when(dealService.lireParCreateur(uuidCreateur)).thenReturn(List.of(dealModele));
        when(mapper.versDTO(dealModele)).thenReturn(dealResponseDto);

        // When
        List<DealResponseDto> resultat = apiAdapter.lireTousByCreateurUuid(uuidCreateur);

        // Then
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertEquals("Dupont Jean", resultat.get(0).getCreateurNom());
        verify(dealService, times(1)).lireParCreateur(uuidCreateur);
        verify(mapper, times(1)).versDTO(dealModele);
    }

    @Test
    void testLireTousByCategorieUuid_DevraitRetournerDealsCategorie() {
        // Given
        when(dealService.lireParCategorie(uuidCategorie)).thenReturn(List.of(dealModele));
        when(mapper.versDTO(dealModele)).thenReturn(dealResponseDto);

        // When
        List<DealResponseDto> resultat = apiAdapter.lireTousByCategorieUuid(uuidCategorie);

        // Then
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertEquals("Viandes", resultat.get(0).getCategorieNom());
        verify(dealService, times(1)).lireParCategorie(uuidCategorie);
        verify(mapper, times(1)).versDTO(dealModele);
    }

    @Test
    void testMettreAJour_DevraitMettreAJourDeal() {
        // Given
        DealDTO dealModifieDTO = DealDTO.builder()
                .uuid(uuidDeal)
                .titre("Filet de boeuf premium - Prix réduit")
                .description("Viande de qualité supérieure - Offre spéciale")
                .prixDeal(new BigDecimal("120.00"))
                .prixPart(new BigDecimal("24.00"))
                .nbParticipants(5)
                .statut(StatutDeal.PUBLIE)
                .build();
        DealModele dealModifieModele = DealModele.builder()
                .uuid(uuidDeal)
                .titre("Filet de boeuf premium - Prix réduit")
                .description("Viande de qualité supérieure - Offre spéciale")
                .prixDeal(new BigDecimal("120.00"))
                .prixPart(new BigDecimal("24.00"))
                .nbParticipants(5)
                .statut(StatutDeal.PUBLIE)
                .build();
        DealResponseDto dealModifieResponseDto = DealResponseDto.builder()
                .uuid(uuidDeal)
                .titre("Filet de boeuf premium - Prix réduit")
                .prixDeal(new BigDecimal("120.00"))
                .prixPart(new BigDecimal("24.00"))
                .build();

        when(mapper.versEntite(dealModifieDTO)).thenReturn(dealModifieModele);
        when(dealService.mettreAJour(eq(uuidDeal), any(DealModele.class))).thenReturn(dealModifieModele);
        when(mapper.versDTO(dealModifieModele)).thenReturn(dealModifieResponseDto);

        // When
        DealResponseDto resultat = apiAdapter.mettreAJour(uuidDeal, dealModifieDTO);

        // Then
        assertNotNull(resultat);
        assertEquals("Filet de boeuf premium - Prix réduit", resultat.getTitre());
        assertEquals(new BigDecimal("120.00"), resultat.getPrixDeal());
        verify(mapper, times(1)).versEntite(dealModifieDTO);
        verify(dealService, times(1)).mettreAJour(eq(uuidDeal), any(DealModele.class));
        verify(mapper, times(1)).versDTO(dealModifieModele);
    }

    @Test
    void testSupprimerParUuid_DevraitSupprimerDeal() {
        // Given
        doNothing().when(dealService).supprimerParUuid(uuidDeal);

        // When
        apiAdapter.supprimerParUuid(uuidDeal);

        // Then
        verify(dealService, times(1)).supprimerParUuid(uuidDeal);
    }

    @Test
    void testCreerDeal_AvecValidationEchoue() {
        // Given
        when(mapper.versEntite(dealDTO)).thenReturn(dealModele);
        when(dealService.creer(dealModele))
                .thenThrow(new IllegalArgumentException("L'attribut titre est obligatoire"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> apiAdapter.creerDeal(dealDTO));
        verify(mapper, times(1)).versEntite(dealDTO);
        verify(dealService, times(1)).creer(dealModele);
        verify(mapper, never()).versDTO(any());
    }

    @Test
    void testLireTous_DevraitRetournerListeVide() {
        // Given
        when(dealService.lireTous()).thenReturn(List.of());

        // When
        List<DealResponseDto> resultat = apiAdapter.lireTous();

        // Then
        assertNotNull(resultat);
        assertTrue(resultat.isEmpty());
        verify(dealService, times(1)).lireTous();
        verify(mapper, never()).versDTO(any());
    }

    @Test
    void testLireTousByStatut_AvecStatutBrouillon() {
        // Given
        DealModele dealBrouillon = DealModele.builder()
                .uuid(UUID.randomUUID())
                .titre("Deal en brouillon")
                .statut(StatutDeal.BROUILLON)
                .build();
        DealResponseDto responseDtoBrouillon = DealResponseDto.builder()
                .uuid(dealBrouillon.getUuid())
                .titre("Deal en brouillon")
                .statut(StatutDeal.BROUILLON)
                .build();

        when(dealService.lireParStatut(StatutDeal.BROUILLON)).thenReturn(List.of(dealBrouillon));
        when(mapper.versDTO(dealBrouillon)).thenReturn(responseDtoBrouillon);

        // When
        List<DealResponseDto> resultat = apiAdapter.lireTousByStatut(StatutDeal.BROUILLON);

        // Then
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertEquals(StatutDeal.BROUILLON, resultat.get(0).getStatut());
        verify(dealService, times(1)).lireParStatut(StatutDeal.BROUILLON);
        verify(mapper, times(1)).versDTO(dealBrouillon);
    }
}
