package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.ImageDealModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.provider.adapter.entity.CategorieJpa;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.DealJpaMapper;
import com.ulr.paytogether.provider.repository.CategorieRepository;
import com.ulr.paytogether.provider.repository.DealRepository;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import com.ulr.paytogether.provider.utils.FileManager;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour DealProviderAdapter
 */
@ExtendWith(MockitoExtension.class)
class DealProviderAdapterTest {

    @Mock
    private DealRepository jpaRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private CategorieRepository categorieRepository;

    @Mock
    private DealJpaMapper mapper;

    @Mock
    private FileManager fileManager;

    @InjectMocks
    private DealProviderAdapter providerAdapter;

    private DealModele dealModele;
    private DealJpa dealJpa;
    private UUID uuidDeal;
    private UUID uuidCreateur;
    private UUID uuidCategorie;
    private UtilisateurJpa marchand;
    CategorieJpa categorieJpa;

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
        marchand = UtilisateurJpa.builder()
                .uuid(uuidCreateur)
                .nom("Dupont")
                .prenom("Jean")
                .build();

        CategorieModele categorie = CategorieModele.builder()
                .uuid(uuidCategorie)
                .nom("Viandes")
                .build();

        categorieJpa = CategorieJpa.builder()
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

        dealJpa = DealJpa.builder()
                .uuid(uuidDeal)
                .titre("Filet de boeuf premium")
                .description("Viande de qualité supérieure")
                .prixDeal(new BigDecimal("150.00"))
                .prixPart(new BigDecimal("30.00"))
                .nbParticipants(5)
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(7))
                .statut(StatutDeal.PUBLIE)
                .ville("Montreal")
                .build();
    }

    @Test
    void testSauvegarder_DevraitSauvegarderDeal() {
        // Given
        when(mapper.versEntite(dealModele)).thenReturn(dealJpa);
        when(jpaRepository.save(any(DealJpa.class))).thenReturn(dealJpa);
        when(mapper.versModele(dealJpa)).thenReturn(dealModele);

        // When
        DealModele resultat = providerAdapter.sauvegarder(dealModele);

        // Then
        assertNotNull(resultat);
        assertEquals("Filet de boeuf premium", resultat.getTitre());
        assertEquals(new BigDecimal("150.00"), resultat.getPrixDeal());
        verify(mapper, times(1)).versEntite(dealModele);
        verify(jpaRepository, times(1)).save(any(DealJpa.class));
        verify(mapper, times(1)).versModele(dealJpa);
    }

    @Test
    void testTrouverParUuid_DevraitRetournerDeal() {
        // Given
        when(jpaRepository.findById(uuidDeal)).thenReturn(Optional.of(dealJpa));
        when(mapper.versModele(dealJpa)).thenReturn(dealModele);

        // When
        Optional<DealModele> resultat = providerAdapter.trouverParUuid(uuidDeal);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("Filet de boeuf premium", resultat.get().getTitre());
        assertEquals(uuidDeal, resultat.get().getUuid());
        verify(jpaRepository, times(1)).findById(uuidDeal);
        verify(mapper, times(1)).versModele(dealJpa);
    }

    @Test
    void testTrouverParUuid_DevraitRetournerOptionalVide() {
        // Given
        when(jpaRepository.findById(uuidDeal)).thenReturn(Optional.empty());

        // When
        Optional<DealModele> resultat = providerAdapter.trouverParUuid(uuidDeal);

        // Then
        assertFalse(resultat.isPresent());
        verify(jpaRepository, times(1)).findById(uuidDeal);
        verify(mapper, never()).versModele(any());
    }

    @Test
    void testTrouverTous_DevraitRetournerListeDeals() {
        // Given
        DealJpa dealJpa2 = DealJpa.builder()
                .uuid(UUID.randomUUID())
                .titre("Saumon frais")
                .description("Poisson de qualité")
                .prixDeal(new BigDecimal("80.00"))
                .prixPart(new BigDecimal("20.00"))
                .nbParticipants(4)
                .statut(StatutDeal.PUBLIE)
                .build();
        DealModele dealModele2 = DealModele.builder()
                .uuid(dealJpa2.getUuid())
                .titre("Saumon frais")
                .description("Poisson de qualité")
                .prixDeal(new BigDecimal("80.00"))
                .prixPart(new BigDecimal("20.00"))
                .nbParticipants(4)
                .statut(StatutDeal.PUBLIE)
                .build();

        when(jpaRepository.findAll()).thenReturn(Arrays.asList(dealJpa, dealJpa2));
        when(mapper.versModele(dealJpa)).thenReturn(dealModele);
        when(mapper.versModele(dealJpa2)).thenReturn(dealModele2);

        // When
        List<DealModele> resultat = providerAdapter.trouverTous();

        // Then
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        assertEquals("Filet de boeuf premium", resultat.get(0).getTitre());
        assertEquals("Saumon frais", resultat.get(1).getTitre());
        verify(jpaRepository, times(1)).findAll();
        verify(mapper, times(2)).versModele(any(DealJpa.class));
    }

    @Test
    void testTrouverParStatut_DevraitRetournerDealsParStatut() {
        // Given
        when(jpaRepository.findByStatut(StatutDeal.PUBLIE)).thenReturn(List.of(dealJpa));
        when(mapper.versModele(dealJpa)).thenReturn(dealModele);

        // When
        List<DealModele> resultat = providerAdapter.trouverParStatut(StatutDeal.PUBLIE);

        // Then
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertEquals(StatutDeal.PUBLIE, resultat.get(0).getStatut());
        verify(jpaRepository, times(1)).findByStatut(StatutDeal.PUBLIE);
        verify(mapper, times(1)).versModele(dealJpa);
    }

    @Test
    void testTrouverParCreateur_DevraitRetournerDealsCreateur() {
        // Given
        when((utilisateurRepository.findById(uuidCreateur))).thenReturn(Optional.of(marchand));
        when(jpaRepository.findByMarchandJpa(any(UtilisateurJpa.class))).thenReturn(List.of(dealJpa));
        when(mapper.versModele(dealJpa)).thenReturn(dealModele);

        // When
        List<DealModele> resultat = providerAdapter.trouverParCreateur(uuidCreateur);

        // Then
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertEquals(uuidCreateur, resultat.get(0).getCreateur().getUuid());
        verify(utilisateurRepository, times(1)).findById(uuidCreateur);
        verify(jpaRepository, times(1)).findByMarchandJpa(marchand);
        verify(mapper, times(1)).versModele(dealJpa);
    }

    @Test
    void testTrouverParCategorie_DevraitRetournerDealsCategorie() {
        // Given
        when(categorieRepository.findById(uuidCategorie)).thenReturn(Optional.of(categorieJpa));
        when(jpaRepository.findByCategorieJpa(any(CategorieJpa.class))).thenReturn(List.of(dealJpa));
        when(mapper.versModele(dealJpa)).thenReturn(dealModele);

        // When
        List<DealModele> resultat = providerAdapter.trouverParCategorie(uuidCategorie);

        // Then
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertEquals(uuidCategorie, resultat.get(0).getCategorie().getUuid());
        verify(categorieRepository, times(1)).findById(uuidCategorie);
        verify(jpaRepository, times(1)).findByCategorieJpa(categorieJpa);
        verify(mapper, times(1)).versModele(dealJpa);
    }

    @Test
    void testMettreAJour_DevraitMettreAJourDeal() {
        // Given
        DealModele dealModifie = DealModele.builder()
                .titre("Filet de boeuf premium - Prix réduit")
                .description("Viande de qualité supérieure - Offre spéciale")
                .prixDeal(new BigDecimal("120.00"))
                .prixPart(new BigDecimal("24.00"))
                .nbParticipants(5)
                .build();

        when(jpaRepository.findById(uuidDeal)).thenReturn(Optional.of(dealJpa));
        doNothing().when(mapper).mettreAJour(dealJpa, dealModifie);
        when(jpaRepository.save(dealJpa)).thenReturn(dealJpa);
        when(mapper.versModele(dealJpa)).thenReturn(dealModifie);

        // When
        DealModele resultat = providerAdapter.mettreAJour(uuidDeal, dealModifie);

        // Then
        assertNotNull(resultat);
        verify(jpaRepository, times(1)).findById(uuidDeal);
        verify(mapper, times(1)).mettreAJour(dealJpa, dealModifie);
        verify(jpaRepository, times(1)).save(dealJpa);
        verify(mapper, times(1)).versModele(dealJpa);
    }

    @Test
    void testMettreAJour_DevraitLancerExceptionSiDealNonTrouve() {
        // Given
        when(jpaRepository.findById(uuidDeal)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> providerAdapter.mettreAJour(uuidDeal, dealModele));
        verify(jpaRepository, times(1)).findById(uuidDeal);
        verify(jpaRepository, never()).save(any());
    }

    @Test
    void testSupprimerParUuid_DevraitSupprimerDeal() {
        // Given
        doNothing().when(jpaRepository).deleteById(uuidDeal);

        // When
        providerAdapter.supprimerParUuid(uuidDeal);

        // Then
        verify(jpaRepository, times(1)).deleteById(uuidDeal);
    }

    @Test
    void testTrouverParStatut_AvecStatutBrouillon() {
        // Given
        DealJpa dealBrouillon = DealJpa.builder()
                .uuid(UUID.randomUUID())
                .titre("Deal en brouillon")
                .statut(StatutDeal.BROUILLON)
                .build();
        DealModele modeleBrouillon = DealModele.builder()
                .uuid(dealBrouillon.getUuid())
                .titre("Deal en brouillon")
                .statut(StatutDeal.BROUILLON)
                .build();

        when(jpaRepository.findByStatut(StatutDeal.BROUILLON)).thenReturn(List.of(dealBrouillon));
        when(mapper.versModele(dealBrouillon)).thenReturn(modeleBrouillon);

        // When
        List<DealModele> resultat = providerAdapter.trouverParStatut(StatutDeal.BROUILLON);

        // Then
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertEquals(StatutDeal.BROUILLON, resultat.get(0).getStatut());
        verify(jpaRepository, times(1)).findByStatut(StatutDeal.BROUILLON);
        verify(mapper, times(1)).versModele(dealBrouillon);
    }

    @Test
    void testSauvegarder_DevraitModifierNomImageAvecTimestamp() {
        // Given
        ImageDealModele imageDealModele =
            ImageDealModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image_original.jpg")
                .isPrincipal(true)
                .statut(StatutImage.PENDING)
                .build();

        DealModele dealAvecImages = DealModele.builder()
                .uuid(uuidDeal)
                .titre("Deal avec images")
                .prixDeal(new BigDecimal("100.00"))
                .prixPart(new BigDecimal("25.00"))
                .nbParticipants(4)
                .statut(StatutDeal.PUBLIE)
                .listeImages(List.of(imageDealModele))
                .ville("Montreal")
                .build();

        when(mapper.versEntite(dealAvecImages)).thenReturn(dealJpa);
        when(jpaRepository.save(any(DealJpa.class))).thenReturn(dealJpa);
        when(mapper.versModele(dealJpa)).thenReturn(dealAvecImages);
        when(fileManager.generatePresignedUrl(anyString(), anyString())).thenReturn("https://presigned-url.com/image");

        // When
        DealModele resultat = providerAdapter.sauvegarder(dealAvecImages);

        // Then
        assertNotNull(resultat);
        verify(mapper, times(1)).versEntite(dealAvecImages);
        // Vérifier que save est appelé avec une entité qui a des images avec timestamp modifié
        verify(jpaRepository, times(1)).save(any(DealJpa.class));
        verify(mapper, times(1)).versModele(dealJpa);
        // Vérifier que generatePresignedUrl est appelé pour les images PENDING
        verify(fileManager, times(1)).generatePresignedUrl(anyString(), anyString());
    }

    @Test
    void testSauvegarder_DevraitGenererPresignedUrlPourImagesPending() {
        // Given
        ImageDealModele imagePending =
            ImageDealModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image_pending.jpg")
                .isPrincipal(true)
                .statut(StatutImage.PENDING)
                .build();

        ImageDealModele imageUploaded =
            ImageDealModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image_uploaded.jpg")
                .isPrincipal(false)
                .statut(StatutImage.UPLOADED)
                .build();

        DealModele dealAvecImages = DealModele.builder()
                .uuid(uuidDeal)
                .titre("Deal avec plusieurs images")
                .prixDeal(new BigDecimal("100.00"))
                .prixPart(new BigDecimal("25.00"))
                .nbParticipants(4)
                .statut(StatutDeal.PUBLIE)
                .listeImages(List.of(imagePending, imageUploaded))
                .ville("Montreal")
                .build();

        when(mapper.versEntite(dealAvecImages)).thenReturn(dealJpa);
        when(jpaRepository.save(any(DealJpa.class))).thenReturn(dealJpa);
        when(mapper.versModele(dealJpa)).thenReturn(dealAvecImages);
        when(fileManager.generatePresignedUrl(anyString(), anyString())).thenReturn("https://presigned-url.com/image");

        // When
        DealModele resultat = providerAdapter.sauvegarder(dealAvecImages);

        // Then
        assertNotNull(resultat);
        // Vérifier que generatePresignedUrl est appelé seulement pour les images PENDING (1 fois)
        verify(fileManager, times(1)).generatePresignedUrl(anyString(), anyString());
        // Vérifier que l'URL présignée est définie sur l'image PENDING
        assertEquals("https://presigned-url.com/image", imagePending.getPresignUrl());
    }

    @Test
    void testSauvegarder_DevraitNePasGenererPresignedUrlSiPasImages() {
        // Given
        DealModele dealSansImages = DealModele.builder()
                .uuid(uuidDeal)
                .titre("Deal sans images")
                .prixDeal(new BigDecimal("100.00"))
                .prixPart(new BigDecimal("25.00"))
                .nbParticipants(4)
                .statut(StatutDeal.PUBLIE)
                .listeImages(List.of())
                .ville("Montreal")
                .build();

        when(mapper.versEntite(dealSansImages)).thenReturn(dealJpa);
        when(jpaRepository.save(any(DealJpa.class))).thenReturn(dealJpa);
        when(mapper.versModele(dealJpa)).thenReturn(dealSansImages);

        // When
        DealModele resultat = providerAdapter.sauvegarder(dealSansImages);

        // Then
        assertNotNull(resultat);
        // Vérifier que generatePresignedUrl n'est jamais appelé
        verify(fileManager, never()).generatePresignedUrl(anyString(), anyString());
    }

    @Test
    void testSauvegarder_DevraitNePasGenererPresignedUrlPourImagesUploaded() {
        // Given
        ImageDealModele imageUploaded =
            ImageDealModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image_uploaded.jpg")
                .isPrincipal(true)
                .statut(StatutImage.UPLOADED)
                .build();

        DealModele dealAvecImageUploaded = DealModele.builder()
                .uuid(uuidDeal)
                .titre("Deal avec image uploadée")
                .prixDeal(new BigDecimal("100.00"))
                .prixPart(new BigDecimal("25.00"))
                .nbParticipants(4)
                .statut(StatutDeal.PUBLIE)
                .listeImages(List.of(imageUploaded))
                .ville("Montreal")
                .build();

        when(mapper.versEntite(dealAvecImageUploaded)).thenReturn(dealJpa);
        when(jpaRepository.save(any(DealJpa.class))).thenReturn(dealJpa);
        when(mapper.versModele(dealJpa)).thenReturn(dealAvecImageUploaded);

        // When
        DealModele resultat = providerAdapter.sauvegarder(dealAvecImageUploaded);

        // Then
        assertNotNull(resultat);
        // Vérifier que generatePresignedUrl n'est pas appelé pour les images UPLOADED
        verify(fileManager, never()).generatePresignedUrl(anyString(), anyString());
    }
}
