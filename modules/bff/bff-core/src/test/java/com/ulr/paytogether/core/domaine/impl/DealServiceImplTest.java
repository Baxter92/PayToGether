package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.validator.DealValidator;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.ImageDealModele;
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
                .dateFin(LocalDateTime.now().plusDays(7))
                .createur(UtilisateurModele.builder().uuid(uuidCreateur).build())
                .categorie(CategorieModele.builder().uuid(uuidCategorie).build())
                .ville("Montreal")
                .build();

        doNothing().when(dealValidator).validerPourMiseAJourPartielle(any(DealModele.class));
        when(dealProvider.mettreAJour(uuidDeal, dealModifie)).thenReturn(dealModifie);

        // When
        DealModele resultat = dealService.mettreAJour(uuidDeal, dealModifie);

        // Then
        assertNotNull(resultat);
        assertEquals("Filet de boeuf premium - Prix réduit", resultat.getTitre());
        verify(dealValidator, times(1)).validerPourMiseAJourPartielle(dealModifie);
        verify(dealProvider, times(1)).mettreAJour(uuidDeal, dealModifie);
    }

    @Test
    void testMettreAJour_DevraitLancerExceptionSiValidationEchoue() {
        // Given
        doThrow(new com.ulr.paytogether.core.exception.ValidationException("deal.ville.obligatoire"))
                .when(dealValidator).validerPourMiseAJourPartielle(any(DealModele.class));

        // When & Then
        com.ulr.paytogether.core.exception.ValidationException exception = assertThrows(
                com.ulr.paytogether.core.exception.ValidationException.class,
                () -> dealService.mettreAJour(uuidDeal, dealModele)
        );

        assertEquals("deal.ville.obligatoire", exception.getErrorCode());
        verify(dealValidator, times(1)).validerPourMiseAJourPartielle(dealModele);
        verify(dealProvider, never()).mettreAJour(any(), any());
    }

    @Test
    void testMettreAJourStatut_DevraitMettreAJourStatut() {
        // Given
        dealModele.setStatut(StatutDeal.BROUILLON);
        DealModele dealMisAJour = DealModele.builder()
                .uuid(uuidDeal)
                .titre(dealModele.getTitre())
                .statut(StatutDeal.PUBLIE)
                .build();

        when(dealProvider.trouverParUuid(uuidDeal)).thenReturn(Optional.of(dealModele));
        doNothing().when(dealValidator).validerTransitionStatut(StatutDeal.BROUILLON, StatutDeal.PUBLIE);
        when(dealProvider.mettreAJourStatut(uuidDeal, StatutDeal.PUBLIE)).thenReturn(dealMisAJour);

        // When
        DealModele resultat = dealService.mettreAJourStatut(uuidDeal, StatutDeal.PUBLIE);

        // Then
        assertNotNull(resultat);
        assertEquals(StatutDeal.PUBLIE, resultat.getStatut());
        verify(dealProvider, times(1)).trouverParUuid(uuidDeal);
        verify(dealValidator, times(1)).validerTransitionStatut(StatutDeal.BROUILLON, StatutDeal.PUBLIE);
        verify(dealProvider, times(1)).mettreAJourStatut(uuidDeal, StatutDeal.PUBLIE);
    }

    @Test
    void testMettreAJourStatut_DevraitLancerExceptionSiDealNonTrouve() {
        // Given
        when(dealProvider.trouverParUuid(uuidDeal)).thenReturn(Optional.empty());

        // When & Then
        com.ulr.paytogether.core.exception.ResourceNotFoundException exception = assertThrows(
                com.ulr.paytogether.core.exception.ResourceNotFoundException.class,
                () -> dealService.mettreAJourStatut(uuidDeal, StatutDeal.PUBLIE)
        );

        assertEquals("deal.non.trouve", exception.getErrorCode());
        verify(dealProvider, times(1)).trouverParUuid(uuidDeal);
        verify(dealValidator, never()).validerTransitionStatut(any(), any());
        verify(dealProvider, never()).mettreAJourStatut(any(), any());
    }

    @Test
    void testMettreAJourStatut_DevraitLancerExceptionSiTransitionInvalide() {
        // Given
        dealModele.setStatut(StatutDeal.EXPIRE);
        when(dealProvider.trouverParUuid(uuidDeal)).thenReturn(Optional.of(dealModele));
        doThrow(new com.ulr.paytogether.core.exception.ValidationException("deal.statut.expire.immuable"))
                .when(dealValidator).validerTransitionStatut(StatutDeal.EXPIRE, StatutDeal.PUBLIE);

        // When & Then
        com.ulr.paytogether.core.exception.ValidationException exception = assertThrows(
                com.ulr.paytogether.core.exception.ValidationException.class,
                () -> dealService.mettreAJourStatut(uuidDeal, StatutDeal.PUBLIE)
        );

        assertEquals("deal.statut.expire.immuable", exception.getErrorCode());
        verify(dealProvider, times(1)).trouverParUuid(uuidDeal);
        verify(dealValidator, times(1)).validerTransitionStatut(StatutDeal.EXPIRE, StatutDeal.PUBLIE);
        verify(dealProvider, never()).mettreAJourStatut(any(), any());
    }

    @Test
    void testMettreAJourImages_DevraitMettreAJourImages() {
        // Given
        com.ulr.paytogether.core.modele.ImageDealModele image1 = com.ulr.paytogether.core.modele.ImageDealModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("nouvelle_image.jpg")
                .isPrincipal(true)
                .statut(com.ulr.paytogether.core.enumeration.StatutImage.PENDING)
                .build();

        DealModele dealAvecNouvellesImages = DealModele.builder()
                .uuid(uuidDeal)
                .listeImages(List.of(image1))
                .build();

        DealModele dealMisAJour = DealModele.builder()
                .uuid(uuidDeal)
                .titre(dealModele.getTitre())
                .listeImages(List.of(image1))
                .build();

        when(dealProvider.trouverParUuid(uuidDeal)).thenReturn(Optional.of(dealModele));
        doNothing().when(dealValidator).validerImages(any(DealModele.class));
        when(dealProvider.mettreAJourImages(uuidDeal, dealAvecNouvellesImages)).thenReturn(dealMisAJour);

        // When
        DealModele resultat = dealService.mettreAJourImages(uuidDeal, dealAvecNouvellesImages);

        // Then
        assertNotNull(resultat);
        assertNotNull(resultat.getListeImages());
        assertEquals(1, resultat.getListeImages().size());
        verify(dealProvider, times(1)).trouverParUuid(uuidDeal);
        verify(dealValidator, times(1)).validerImages(dealAvecNouvellesImages);
        verify(dealProvider, times(1)).mettreAJourImages(uuidDeal, dealAvecNouvellesImages);
    }

    @Test
    void testMettreAJourImages_DevraitLancerExceptionSiDealNonTrouve() {
        // Given
        DealModele dealAvecImages = DealModele.builder()
                .uuid(uuidDeal)
                .listeImages(List.of())
                .build();

        when(dealProvider.trouverParUuid(uuidDeal)).thenReturn(Optional.empty());

        // When & Then
        com.ulr.paytogether.core.exception.ResourceNotFoundException exception = assertThrows(
                com.ulr.paytogether.core.exception.ResourceNotFoundException.class,
                () -> dealService.mettreAJourImages(uuidDeal, dealAvecImages)
        );

        assertEquals("deal.non.trouve", exception.getErrorCode());
        verify(dealProvider, times(1)).trouverParUuid(uuidDeal);
        verify(dealValidator, never()).validerImages(any());
        verify(dealProvider, never()).mettreAJourImages(any(), any());
    }

    @Test
    void testMettreAJourImages_DevraitLancerExceptionSiValidationEchoue() {
        // Given
        DealModele dealSansImagePrincipale = DealModele.builder()
                .uuid(uuidDeal)
                .listeImages(List.of(
                        com.ulr.paytogether.core.modele.ImageDealModele.builder()
                                .uuid(UUID.randomUUID())
                                .urlImage("image1.jpg")
                                .isPrincipal(false)
                                .build()
                ))
                .build();

        when(dealProvider.trouverParUuid(uuidDeal)).thenReturn(Optional.of(dealModele));
        doThrow(new com.ulr.paytogether.core.exception.ValidationException("deal.image.principale.manquante"))
                .when(dealValidator).validerImages(dealSansImagePrincipale);

        // When & Then
        com.ulr.paytogether.core.exception.ValidationException exception = assertThrows(
                com.ulr.paytogether.core.exception.ValidationException.class,
                () -> dealService.mettreAJourImages(uuidDeal, dealSansImagePrincipale)
        );

        assertEquals("deal.image.principale.manquante", exception.getErrorCode());
        verify(dealProvider, times(1)).trouverParUuid(uuidDeal);
        verify(dealValidator, times(1)).validerImages(dealSansImagePrincipale);
        verify(dealProvider, never()).mettreAJourImages(any(), any());
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

    @Test
    void testMettreAJour_AvecImages_DevraitMettreAJourDealEtImages() {
        // Given
        ImageDealModele nouvelleImage = ImageDealModele.builder()
                .uuid(null) // Nouvelle image
                .urlImage("nouvelle_image.jpg")
                .isPrincipal(true)
                .statut(StatutImage.PENDING)
                .build();

        DealModele dealAvecImages = DealModele.builder()
                .uuid(uuidDeal)
                .titre("Deal modifié")
                .description("Description modifiée")
                .prixDeal(new BigDecimal("150.00"))
                .prixPart(new BigDecimal("30.00"))
                .nbParticipants(5)
                .dateFin(LocalDateTime.now().plusDays(7))
                .createur(UtilisateurModele.builder().uuid(uuidCreateur).build())
                .categorie(CategorieModele.builder().uuid(uuidCategorie).build())
                .ville("Montreal")
                .listeImages(List.of(nouvelleImage)) // Images présentes
                .build();

        DealModele dealMisAJourSansImages = DealModele.builder()
                .uuid(uuidDeal)
                .titre("Deal modifié")
                .listeImages(null) // Sans images après mise à jour générale
                .build();

        ImageDealModele imageAvecPresignUrl = ImageDealModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("deals/nouvelle_image_1707988800000.jpg")
                .isPrincipal(true)
                .statut(StatutImage.PENDING)
                .presignUrl("https://minio.../put-url")
                .build();

        DealModele dealAvecNouvellesImages = DealModele.builder()
                .uuid(uuidDeal)
                .listeImages(List.of(imageAvecPresignUrl))
                .build();

        doNothing().when(dealValidator).validerPourMiseAJourPartielle(any(DealModele.class));
        doNothing().when(dealValidator).validerImages(any(DealModele.class));
        when(dealProvider.mettreAJour(eq(uuidDeal), any(DealModele.class))).thenReturn(dealMisAJourSansImages);
        when(dealProvider.mettreAJourImages(eq(uuidDeal), any(DealModele.class))).thenReturn(dealAvecNouvellesImages);

        // When
        DealModele resultat = dealService.mettreAJour(uuidDeal, dealAvecImages);

        // Then
        assertNotNull(resultat);
        assertNotNull(resultat.getListeImages());
        assertEquals(1, resultat.getListeImages().size());
        assertEquals("https://minio.../put-url", resultat.getListeImages().get(0).getPresignUrl());

        // Vérifier que les deux méthodes ont été appelées
        verify(dealValidator, times(1)).validerPourMiseAJourPartielle(dealAvecImages);
        verify(dealValidator, times(1)).validerImages(dealAvecImages);
        verify(dealProvider, times(1)).mettreAJour(eq(uuidDeal), any(DealModele.class));
        verify(dealProvider, times(1)).mettreAJourImages(eq(uuidDeal), any(DealModele.class));
    }

    @Test
    void testMettreAJour_SansImages_DevraitMettreAJourSeulementDeal() {
        // Given
        DealModele dealSansImages = DealModele.builder()
                .uuid(uuidDeal)
                .titre("Deal modifié")
                .description("Description modifiée")
                .prixDeal(new BigDecimal("150.00"))
                .prixPart(new BigDecimal("30.00"))
                .nbParticipants(5)
                .dateFin(LocalDateTime.now().plusDays(7))
                .createur(UtilisateurModele.builder().uuid(uuidCreateur).build())
                .categorie(CategorieModele.builder().uuid(uuidCategorie).build())
                .ville("Montreal")
                .listeImages(null) // Pas d'images
                .build();

        doNothing().when(dealValidator).validerPourMiseAJourPartielle(any(DealModele.class));
        when(dealProvider.mettreAJour(eq(uuidDeal), any(DealModele.class))).thenReturn(dealSansImages);

        // When
        DealModele resultat = dealService.mettreAJour(uuidDeal, dealSansImages);

        // Then
        assertNotNull(resultat);
        assertEquals( 0, resultat.getListeImages().size());

        // Vérifier que seul le provider.mettreAJour a été appelé (pas mettreAJourImages)
        verify(dealValidator, times(1)).validerPourMiseAJourPartielle(dealSansImages);
        verify(dealValidator, never()).validerImages(any());
        verify(dealProvider, times(1)).mettreAJour(eq(uuidDeal), any(DealModele.class));
        verify(dealProvider, never()).mettreAJourImages(any(), any());
    }
}
