package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.validator.DealValidator;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.core.provider.DealProvider;
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
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour DealServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class DealServiceImplTest {

    @Mock
    private DealProvider dealProvider;

    @Mock
    private DealValidator dealValidator;

    @InjectMocks
    private DealServiceImpl dealService;

    private DealModele dealModele;
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
                .listeImages(List.of())
                .ville("Montreal")
                .build();
    }

    @Test
    void testCreer_DevraitCreerDeal() {
        // Given
        doNothing().when(dealValidator).valider(any(DealModele.class));
        when(dealProvider.sauvegarder(any(DealModele.class))).thenReturn(dealModele);

        // When
        DealModele resultat = dealService.creer(dealModele);

        // Then
        assertNotNull(resultat);
        assertEquals("Filet de boeuf premium", resultat.getTitre());
        assertEquals(new BigDecimal("150.00"), resultat.getPrixDeal());
        verify(dealValidator, times(1)).valider(dealModele);
        verify(dealProvider, times(1)).sauvegarder(dealModele);
    }

    @Test
    void testCreer_DevraitLancerExceptionSiValidationEchoue() {
        // Given
        doThrow(new IllegalArgumentException("L'attribut titre est obligatoire"))
                .when(dealValidator).valider(any(DealModele.class));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dealService.creer(dealModele)
        );

        assertEquals("L'attribut titre est obligatoire", exception.getMessage());
        verify(dealValidator, times(1)).valider(dealModele);
        verify(dealProvider, never()).sauvegarder(any());
    }

    @Test
    void testLireParUuid_DevraitRetournerDeal() {
        // Given
        when(dealProvider.trouverParUuid(uuidDeal)).thenReturn(Optional.of(dealModele));

        // When
        Optional<DealModele> resultat = dealService.lireParUuid(uuidDeal);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals(uuidDeal, resultat.get().getUuid());
        assertEquals("Filet de boeuf premium", resultat.get().getTitre());
        verify(dealProvider, times(1)).trouverParUuid(uuidDeal);
    }

    @Test
    void testLireParUuid_DevraitRetournerOptionalVide() {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        when(dealProvider.trouverParUuid(uuidInexistant)).thenReturn(Optional.empty());

        // When
        Optional<DealModele> resultat = dealService.lireParUuid(uuidInexistant);

        // Then
        assertFalse(resultat.isPresent());
        verify(dealProvider, times(1)).trouverParUuid(uuidInexistant);
    }

    @Test
    void testLireTous_DevraitRetournerTousLesDeals() {
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

        when(dealProvider.trouverTous()).thenReturn(Arrays.asList(dealModele, dealModele2));

        // When
        List<DealModele> resultat = dealService.lireTous();

        // Then
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        assertEquals("Filet de boeuf premium", resultat.get(0).getTitre());
        assertEquals("Saumon frais", resultat.get(1).getTitre());
        verify(dealProvider, times(1)).trouverTous();
    }

    @Test
    void testLireParStatut_DevraitRetournerDealsParStatut() {
        // Given
        when(dealProvider.trouverParStatut(StatutDeal.PUBLIE)).thenReturn(List.of(dealModele));

        // When
        List<DealModele> resultat = dealService.lireParStatut(StatutDeal.PUBLIE);

        // Then
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertEquals(StatutDeal.PUBLIE, resultat.get(0).getStatut());
        verify(dealProvider, times(1)).trouverParStatut(StatutDeal.PUBLIE);
    }

    @Test
    void testLireParCreateur_DevraitRetournerDealsCreateur() {
        // Given
        when(dealProvider.trouverParCreateur(uuidCreateur)).thenReturn(List.of(dealModele));

        // When
        List<DealModele> resultat = dealService.lireParCreateur(uuidCreateur);

        // Then
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertEquals(uuidCreateur, resultat.get(0).getCreateur().getUuid());
        verify(dealProvider, times(1)).trouverParCreateur(uuidCreateur);
    }

    @Test
    void testLireParCategorie_DevraitRetournerDealsCategorie() {
        // Given
        when(dealProvider.trouverParCategorie(uuidCategorie)).thenReturn(List.of(dealModele));

        // When
        List<DealModele> resultat = dealService.lireParCategorie(uuidCategorie);

        // Then
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertEquals(uuidCategorie, resultat.get(0).getCategorie().getUuid());
        verify(dealProvider, times(1)).trouverParCategorie(uuidCategorie);
    }

    @Test
    void testMettreAJour_DevraitMettreAJourDeal() {
        // Given
        DealModele dealModifie = DealModele.builder()
                .uuid(uuidDeal)
                .titre("Filet de boeuf premium - Prix réduit")
                .description("Viande de qualité supérieure")
                .prixDeal(new BigDecimal("120.00"))
                .prixPart(new BigDecimal("24.00"))
                .nbParticipants(5)
                .statut(StatutDeal.PUBLIE)
                .build();

        doNothing().when(dealValidator).valider(any(DealModele.class));
        when(dealProvider.mettreAJour(uuidDeal, dealModifie)).thenReturn(dealModifie);

        // When
        DealModele resultat = dealService.mettreAJour(uuidDeal, dealModifie);

        // Then
        assertNotNull(resultat);
        assertEquals("Filet de boeuf premium - Prix réduit", resultat.getTitre());
        verify(dealValidator, times(1)).valider(dealModifie);
        verify(dealProvider, times(1)).mettreAJour(uuidDeal, dealModifie);
    }

    @Test
    void testMettreAJour_DevraitLancerExceptionSiValidationEchoue() {
        // Given
        doThrow(new IllegalArgumentException("L'attribut ville est obligatoire"))
                .when(dealValidator).valider(any(DealModele.class));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dealService.mettreAJour(uuidDeal, dealModele)
        );

        assertEquals("L'attribut ville est obligatoire", exception.getMessage());
        verify(dealValidator, times(1)).valider(dealModele);
        verify(dealProvider, never()).mettreAJour(any(), any());
    }

    @Test
    void testSupprimerParUuid_DevraitSupprimerDeal() {
        // Given
        doNothing().when(dealProvider).supprimerParUuid(uuidDeal);

        // When
        dealService.supprimerParUuid(uuidDeal);

        // Then
        verify(dealProvider, times(1)).supprimerParUuid(uuidDeal);
    }

    @Test
    void testSupprimerParUuid_AvecUuidNull() {
        // Given
        doThrow(new IllegalArgumentException("L'UUID ne peut pas être null"))
                .when(dealProvider).supprimerParUuid(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> dealService.supprimerParUuid(null));
        verify(dealProvider, times(1)).supprimerParUuid(null);
    }
}
