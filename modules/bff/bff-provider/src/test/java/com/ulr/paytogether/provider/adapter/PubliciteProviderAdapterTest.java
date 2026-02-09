package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.ImageModele;
import com.ulr.paytogether.core.modele.PubliciteModele;
import com.ulr.paytogether.provider.adapter.entity.PubliciteJpa;
import com.ulr.paytogether.provider.adapter.mapper.PubliciteJpaMapper;
import com.ulr.paytogether.provider.repository.PubliciteRepository;
import com.ulr.paytogether.provider.utils.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
 * Tests unitaires pour PubliciteProviderAdapter
 */
@ExtendWith(MockitoExtension.class)
class PubliciteProviderAdapterTest {

    @Mock
    private PubliciteRepository jpaRepository;

    @Mock
    private PubliciteJpaMapper mapper;

    @Mock
    private FileManager fileManager;

    @InjectMocks
    private PubliciteProviderAdapter providerAdapter;

    private PubliciteModele publiciteModele;
    private PubliciteJpa publiciteJpa;
    private UUID uuidPublicite;

    @BeforeEach
    void setUp() {
        uuidPublicite = UUID.randomUUID();

        publiciteModele = PubliciteModele.builder()
                .uuid(uuidPublicite)
                .titre("Publicité de test")
                .description("Description")
                .lienExterne("https://example.com")
                .listeImages(List.of(ImageModele.builder().urlImage("image1.jpg").build()))
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        publiciteJpa = PubliciteJpa.builder()
                .uuid(uuidPublicite)
                .titre("Publicité de test")
                .description("Description")
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();
}

    @Test
    void testSauvegarder_DevraitSauvegarderPublicite() {
        // Given
        when(mapper.versEntite(publiciteModele)).thenReturn(publiciteJpa);
        when(jpaRepository.save(publiciteJpa)).thenReturn(publiciteJpa);
        when(mapper.versModele(publiciteJpa)).thenReturn(publiciteModele);

        // When
        PubliciteModele resultat = providerAdapter.sauvegarder(publiciteModele);

        // Then
        assertNotNull(resultat);
        assertEquals("Publicité de test", resultat.getTitre());
        verify(mapper, times(1)).versEntite(publiciteModele);
        verify(jpaRepository, times(1)).save(publiciteJpa);
        verify(mapper, times(1)).versModele(publiciteJpa);
    }

    @Test
    void testTrouverParUuid_DevraitRetournerPublicite() {
        // Given
        when(jpaRepository.findById(uuidPublicite)).thenReturn(Optional.of(publiciteJpa));
        when(mapper.versModele(publiciteJpa)).thenReturn(publiciteModele);

        // When
        Optional<PubliciteModele> resultat = providerAdapter.trouverParUuid(uuidPublicite);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("Publicité de test", resultat.get().getTitre());
        verify(jpaRepository, times(1)).findById(uuidPublicite);
        verify(mapper, times(1)).versModele(publiciteJpa);
    }

    @Test
    void testTrouverParUuid_DevraitRetournerOptionalVide() {
        // Given
        when(jpaRepository.findById(uuidPublicite)).thenReturn(Optional.empty());

        // When
        Optional<PubliciteModele> resultat = providerAdapter.trouverParUuid(uuidPublicite);

        // Then
        assertFalse(resultat.isPresent());
        verify(jpaRepository, times(1)).findById(uuidPublicite);
        verify(mapper, never()).versModele(any());
    }

    @Test
    void testTrouverTous_DevraitRetournerListePublicites() {
        // Given
        PubliciteJpa publiciteJpa2 = PubliciteJpa.builder()
                .uuid(UUID.randomUUID())
                .titre("Publicité 2")
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(15))
                .active(false)
                .build();
        PubliciteModele publiciteModele2 = PubliciteModele.builder()
                .uuid(publiciteJpa2.getUuid())
                .titre("Publicité 2")
                .dateDebut(publiciteJpa2.getDateDebut())
                .dateFin(publiciteJpa2.getDateFin())
                .active(false)
                .build();

        when(jpaRepository.findAll()).thenReturn(Arrays.asList(publiciteJpa, publiciteJpa2));
        when(mapper.versModele(publiciteJpa)).thenReturn(publiciteModele);
        when(mapper.versModele(publiciteJpa2)).thenReturn(publiciteModele2);

        // When
        List<PubliciteModele> resultat = providerAdapter.trouverTous();

        // Then
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        verify(jpaRepository, times(1)).findAll();
        verify(mapper, times(2)).versModele(any(PubliciteJpa.class));
    }

    @Test
    void testTrouverActives_DevraitRetournerPublicitesActives() {
        // Given
        when(jpaRepository.findByActiveTrue()).thenReturn(List.of(publiciteJpa));
        when(mapper.versModele(publiciteJpa)).thenReturn(publiciteModele);

        // When
        List<PubliciteModele> resultat = providerAdapter.trouverActives();

        // Then
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertTrue(resultat.get(0).getActive());
        verify(jpaRepository, times(1)).findByActiveTrue();
        verify(mapper, times(1)).versModele(publiciteJpa);
    }

    @Test
    void testMettreAJour_DevraitMettreAJourPublicite() {
        // Given
        PubliciteModele publiciteModifiee = PubliciteModele.builder()
                .titre("Titre modifié")
                .description("Description modifiée")
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(45))
                .active(false)
                .build();

        when(jpaRepository.findById(uuidPublicite)).thenReturn(Optional.of(publiciteJpa));
        doNothing().when(mapper).mettreAJour(publiciteJpa, publiciteModifiee);
        when(jpaRepository.save(publiciteJpa)).thenReturn(publiciteJpa);
        when(mapper.versModele(publiciteJpa)).thenReturn(publiciteModifiee);

        // When
        PubliciteModele resultat = providerAdapter.mettreAJour(uuidPublicite, publiciteModifiee);

        // Then
        assertNotNull(resultat);
        verify(jpaRepository, times(1)).findById(uuidPublicite);
        verify(mapper, times(1)).mettreAJour(publiciteJpa, publiciteModifiee);
        verify(jpaRepository, times(1)).save(publiciteJpa);
        verify(mapper, times(1)).versModele(publiciteJpa);
    }

    @Test
    void testMettreAJour_DevraitLancerExceptionSiPubliciteNonTrouvee() {
        // Given
        when(jpaRepository.findById(uuidPublicite)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () ->
            providerAdapter.mettreAJour(uuidPublicite, publiciteModele));
        verify(jpaRepository, times(1)).findById(uuidPublicite);
        verify(jpaRepository, never()).save(any());
    }

    @Test
    void testSauvegarder_DevraitSupprimerPublicite() {
        // Given
        doNothing().when(jpaRepository).deleteById(uuidPublicite);

        // When
        providerAdapter.supprimerParUuid(uuidPublicite);

        // Then
        verify(jpaRepository, times(1)).deleteById(uuidPublicite);
    }

    // ==================== Tests pour la gestion du FileManager ====================

    @Test
    void testSauvegarder_AvecImages_DevraitGenererUrlPresignee() {
        // Given
        ImageModele imagePending = ImageModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image_pending.jpg")
                .statut(StatutImage.PENDING)
                .build();

        PubliciteModele publiciteAvecImages = PubliciteModele.builder()
                .uuid(uuidPublicite)
                .titre("Publicité avec images")
                .listeImages(List.of(imagePending))
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        when(mapper.versEntite(publiciteAvecImages)).thenReturn(publiciteJpa);
        when(jpaRepository.save(any(PubliciteJpa.class))).thenReturn(publiciteJpa);
        when(mapper.versModele(publiciteJpa)).thenReturn(publiciteAvecImages);
        when(fileManager.generatePresignedUrl(anyString(), anyString())).thenReturn("https://presigned-url.com/image");

        // When
        PubliciteModele resultat = providerAdapter.sauvegarder(publiciteAvecImages);

        // Then
        assertNotNull(resultat);
        verify(fileManager, times(1)).generatePresignedUrl(anyString(), anyString());
        verify(jpaRepository, times(1)).save(any(PubliciteJpa.class));
        assertEquals("https://presigned-url.com/image", imagePending.getPresignUrl());
    }

    @Test
    void testSauvegarder_SansImages_NePasGenererUrlPresignee() {
        // Given
        when(mapper.versEntite(publiciteModele)).thenReturn(publiciteJpa);
        when(jpaRepository.save(publiciteJpa)).thenReturn(publiciteJpa);
        when(mapper.versModele(publiciteJpa)).thenReturn(publiciteModele);

        // When
        PubliciteModele resultat = providerAdapter.sauvegarder(publiciteModele);

        // Then
        assertNotNull(resultat);
        verify(fileManager, never()).generatePresignedUrl(anyString(), anyString());
    }

    @Test
    void testSauvegarder_AvecImagesUploaded_NePasGenererUrlPresignee() {
        // Given
        ImageModele imageUploaded = ImageModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image_uploaded.jpg")
                .statut(StatutImage.UPLOADED)
                .build();

        PubliciteModele publiciteAvecImageUploaded = PubliciteModele.builder()
                .uuid(uuidPublicite)
                .titre("Publicité avec image uploaded")
                .listeImages(List.of(imageUploaded))
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        when(mapper.versEntite(publiciteAvecImageUploaded)).thenReturn(publiciteJpa);
        when(jpaRepository.save(any(PubliciteJpa.class))).thenReturn(publiciteJpa);
        when(mapper.versModele(publiciteJpa)).thenReturn(publiciteAvecImageUploaded);

        // When
        PubliciteModele resultat = providerAdapter.sauvegarder(publiciteAvecImageUploaded);

        // Then
        assertNotNull(resultat);
        verify(fileManager, never()).generatePresignedUrl(anyString(), anyString());
    }

    @Test
    void testMettreAJour_AvecNouvelleImage_DevraitMettreStatutPending() {
        // Given
        ImageModele nouvelleImage = ImageModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("nouvelle_image.jpg")
                .statut(StatutImage.PENDING)
                .build();

        com.ulr.paytogether.provider.adapter.entity.ImageJpa ancienneImageJpa =
            com.ulr.paytogether.provider.adapter.entity.ImageJpa.builder()
                .uuid(nouvelleImage.getUuid())
                .urlImage("ancienne_image.jpg")
                .statut(StatutImage.UPLOADED)
                .build();

        PubliciteModele publiciteAvecNouvelleImage = PubliciteModele.builder()
                .uuid(uuidPublicite)
                .titre("Publicité modifiée")
                .listeImages(List.of(nouvelleImage))
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        PubliciteJpa publiciteJpaExistante = PubliciteJpa.builder()
                .uuid(uuidPublicite)
                .titre("Publicité")
                .listeImages(List.of(ancienneImageJpa))
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        when(jpaRepository.findById(uuidPublicite)).thenReturn(Optional.of(publiciteJpaExistante));
        doNothing().when(mapper).mettreAJour(publiciteJpaExistante, publiciteAvecNouvelleImage);
        when(jpaRepository.save(publiciteJpaExistante)).thenReturn(publiciteJpaExistante);
        when(mapper.versModele(publiciteJpaExistante)).thenReturn(publiciteAvecNouvelleImage);
        when(fileManager.generatePresignedUrl(anyString(), anyString())).thenReturn("https://presigned-url.com/nouvelle-image");

        // When
        PubliciteModele resultat = providerAdapter.mettreAJour(uuidPublicite, publiciteAvecNouvelleImage);

        // Then
        assertNotNull(resultat);
        verify(jpaRepository, times(1)).save(publiciteJpaExistante);
        verify(fileManager, times(1)).generatePresignedUrl(anyString(), anyString());
        assertEquals(StatutImage.PENDING, ancienneImageJpa.getStatut());
    }

    @Test
    void testMettreAJour_SansChangementImage_NePasModifierStatut() {
        // Given
        ImageModele imageExistante = ImageModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image_existante.jpg")
                .statut(StatutImage.UPLOADED)
                .build();

        com.ulr.paytogether.provider.adapter.entity.ImageJpa imageJpa =
            com.ulr.paytogether.provider.adapter.entity.ImageJpa.builder()
                .uuid(imageExistante.getUuid())
                .urlImage("image_existante.jpg")
                .statut(StatutImage.UPLOADED)
                .build();

        PubliciteModele publiciteModifiee = PubliciteModele.builder()
                .uuid(uuidPublicite)
                .titre("Publicité modifiée")
                .listeImages(List.of(imageExistante))
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        PubliciteJpa publiciteJpaExistante = PubliciteJpa.builder()
                .uuid(uuidPublicite)
                .titre("Publicité")
                .listeImages(List.of(imageJpa))
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        when(jpaRepository.findById(uuidPublicite)).thenReturn(Optional.of(publiciteJpaExistante));
        doNothing().when(mapper).mettreAJour(publiciteJpaExistante, publiciteModifiee);
        when(jpaRepository.save(publiciteJpaExistante)).thenReturn(publiciteJpaExistante);
        when(mapper.versModele(publiciteJpaExistante)).thenReturn(publiciteModifiee);

        // When
        PubliciteModele resultat = providerAdapter.mettreAJour(uuidPublicite, publiciteModifiee);

        // Then
        assertNotNull(resultat);
        verify(jpaRepository, times(1)).save(publiciteJpaExistante);
        assertEquals(StatutImage.UPLOADED, imageJpa.getStatut());
        verify(fileManager, never()).generatePresignedUrl(anyString(), anyString());
    }
}
