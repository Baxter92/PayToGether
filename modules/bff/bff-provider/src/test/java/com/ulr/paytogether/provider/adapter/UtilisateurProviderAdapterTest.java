package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.enumeration.RoleUtilisateur;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.enumeration.StatutUtilisateur;
import com.ulr.paytogether.core.modele.ImageUtilisateurModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.provider.adapter.entity.ImageUtilisateurJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.ImageUtilisateurJpaMapper;
import com.ulr.paytogether.provider.adapter.mapper.UtilisateurJpaMapper;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import com.ulr.paytogether.provider.utils.FileManager;
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
import static org.mockito.ArgumentMatchers.anyString;
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

    @Mock
    private ImageUtilisateurJpaMapper imageMapper;

    @Mock
    private FileManager fileManager;

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
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.UTILISATEUR)
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

    // ==================== Tests pour la gestion de la photo de profil ====================

    @Test
    void testSauvegarder_AvecPhotoProfil_DevraitGenererUrlPresignee() {
        // Given
        ImageUtilisateurModele imageModele = ImageUtilisateurModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("photo_profil.jpg")
                .statut(StatutImage.PENDING)
                .build();

        ImageUtilisateurJpa imageJpa = ImageUtilisateurJpa.builder()
                .uuid(imageModele.getUuid())
                .urlImage("photo_profil.jpg")
                .statut(StatutImage.PENDING)
                .build();

        UtilisateurModele utilisateurAvecPhoto = UtilisateurModele.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .motDePasse("motDePasseHash")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.UTILISATEUR)
                .photoProfil(imageModele)
                .build();

        UtilisateurJpa utilisateurJpaAvecPhoto = UtilisateurJpa.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .motDePasse("motDePasseHash")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.UTILISATEUR)
                .photoProfil(imageJpa)
                .build();

        when(mapper.versEntite(utilisateurAvecPhoto)).thenReturn(utilisateurJpaAvecPhoto);
        when(jpaRepository.save(any(UtilisateurJpa.class))).thenReturn(utilisateurJpaAvecPhoto);
        when(mapper.versModele(utilisateurJpaAvecPhoto)).thenReturn(utilisateurAvecPhoto);
        when(fileManager.generatePresignedUrl(anyString())).thenReturn("https://presigned-url.com/photo");

        // When
        UtilisateurModele resultat = providerAdapter.sauvegarder(utilisateurAvecPhoto);

        // Then
        assertNotNull(resultat);
        assertNotNull(resultat.getPhotoProfil());
        verify(fileManager, times(1)).generatePresignedUrl(anyString());
        verify(jpaRepository, times(1)).save(any(UtilisateurJpa.class));
        assertEquals("https://presigned-url.com/photo", utilisateurAvecPhoto.getPresignUrlPhotoProfil());
    }

    @Test
    void testSauvegarder_SansPhotoProfil_NePasGenererUrlPresignee() {
        // Given
        when(mapper.versEntite(utilisateurModele)).thenReturn(utilisateurJpa);
        when(jpaRepository.save(utilisateurJpa)).thenReturn(utilisateurJpa);
        when(mapper.versModele(utilisateurJpa)).thenReturn(utilisateurModele);

        // When
        UtilisateurModele resultat = providerAdapter.sauvegarder(utilisateurModele);

        // Then
        assertNotNull(resultat);
        verify(fileManager, never()).generatePresignedUrl(anyString());
        verify(jpaRepository, times(1)).save(utilisateurJpa);
    }

    @Test
    void testSauvegarder_AvecPhotoProfilUploaded_NePasGenererUrlPresignee() {
        // Given
        ImageUtilisateurModele imageUploadedModele = ImageUtilisateurModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("photo_uploaded.jpg")
                .statut(StatutImage.UPLOADED)
                .build();

        ImageUtilisateurJpa imageUploadedJpa = ImageUtilisateurJpa.builder()
                .uuid(imageUploadedModele.getUuid())
                .urlImage("photo_uploaded.jpg")
                .statut(StatutImage.UPLOADED)
                .build();

        UtilisateurModele utilisateurAvecPhotoUploaded = UtilisateurModele.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .photoProfil(imageUploadedModele)
                .build();

        UtilisateurJpa utilisateurJpaAvecPhotoUploaded = UtilisateurJpa.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .photoProfil(imageUploadedJpa)
                .build();

        when(mapper.versEntite(utilisateurAvecPhotoUploaded)).thenReturn(utilisateurJpaAvecPhotoUploaded);
        when(jpaRepository.save(any(UtilisateurJpa.class))).thenReturn(utilisateurJpaAvecPhotoUploaded);
        when(mapper.versModele(utilisateurJpaAvecPhotoUploaded)).thenReturn(utilisateurAvecPhotoUploaded);

        // When
        UtilisateurModele resultat = providerAdapter.sauvegarder(utilisateurAvecPhotoUploaded);

        // Then
        assertNotNull(resultat);
        verify(fileManager, times(1)).generatePresignedUrl(anyString());
    }

    @Test
    void testMettreAJour_AvecNouvellePhotoProfil_DevraitMettreStatutPending() {
        // Given
        ImageUtilisateurModele nouvelleImage = ImageUtilisateurModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("nouvelle_photo.jpg")
                .statut(StatutImage.PENDING)
                .build();

        ImageUtilisateurJpa ancienneImageJpa = ImageUtilisateurJpa.builder()
                .uuid(UUID.randomUUID())
                .urlImage("ancienne_photo.jpg")
                .statut(StatutImage.UPLOADED)
                .build();

        UtilisateurModele utilisateurAvecNouvellePhoto = UtilisateurModele.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .photoProfil(nouvelleImage)
                .build();

        UtilisateurJpa utilisateurJpaExistant = UtilisateurJpa.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .photoProfil(ancienneImageJpa)
                .build();

        when(jpaRepository.findById(uuidUtilisateur)).thenReturn(Optional.of(utilisateurJpaExistant));
        doNothing().when(mapper).mettreAJour(utilisateurJpaExistant, utilisateurAvecNouvellePhoto);
        when(jpaRepository.save(utilisateurJpaExistant)).thenReturn(utilisateurJpaExistant);
        when(mapper.versModele(utilisateurJpaExistant)).thenReturn(utilisateurAvecNouvellePhoto);
        when(fileManager.generatePresignedUrl(anyString())).thenReturn("https://presigned-url.com/nouvelle-photo");

        // When
        UtilisateurModele resultat = providerAdapter.mettreAJour(uuidUtilisateur, utilisateurAvecNouvellePhoto);

        // Then
        assertNotNull(resultat);
        verify(jpaRepository, times(1)).save(utilisateurJpaExistant);
        verify(fileManager, times(1)).generatePresignedUrl(anyString());
        assertEquals(StatutImage.PENDING, ancienneImageJpa.getStatut());
    }

    @Test
    void testMettreAJour_SansChangementPhotoProfil_NePasModifierStatut() {
        // Given
        ImageUtilisateurModele imageModele = ImageUtilisateurModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("photo_existante.jpg")
                .statut(StatutImage.UPLOADED)
                .build();

        ImageUtilisateurJpa imageJpa = ImageUtilisateurJpa.builder()
                .uuid(imageModele.getUuid())
                .urlImage("photo_existante.jpg")
                .statut(StatutImage.UPLOADED)
                .build();

        UtilisateurModele utilisateurAvecPhoto = UtilisateurModele.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .photoProfil(imageModele)
                .build();

        UtilisateurJpa utilisateurJpaExistant = UtilisateurJpa.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .photoProfil(imageJpa)
                .build();

        when(jpaRepository.findById(uuidUtilisateur)).thenReturn(Optional.of(utilisateurJpaExistant));
        doNothing().when(mapper).mettreAJour(utilisateurJpaExistant, utilisateurAvecPhoto);
        when(jpaRepository.save(utilisateurJpaExistant)).thenReturn(utilisateurJpaExistant);
        when(mapper.versModele(utilisateurJpaExistant)).thenReturn(utilisateurAvecPhoto);

        // When
        UtilisateurModele resultat = providerAdapter.mettreAJour(uuidUtilisateur, utilisateurAvecPhoto);

        // Then
        assertNotNull(resultat);
        verify(jpaRepository, times(1)).save(utilisateurJpaExistant);
        // Le statut ne doit pas être modifié car l'URL est la même
        assertEquals(StatutImage.UPLOADED, imageJpa.getStatut());
        verify(fileManager, never()).generatePresignedUrl(anyString());
    }

    @Test
    void testMettreAJour_SansPhotoProfil_NePasGenererUrl() {
        // Given
        when(jpaRepository.findById(uuidUtilisateur)).thenReturn(Optional.of(utilisateurJpa));
        doNothing().when(mapper).mettreAJour(utilisateurJpa, utilisateurModele);
        when(jpaRepository.save(utilisateurJpa)).thenReturn(utilisateurJpa);
        when(mapper.versModele(utilisateurJpa)).thenReturn(utilisateurModele);

        // When
        UtilisateurModele resultat = providerAdapter.mettreAJour(uuidUtilisateur, utilisateurModele);

        // Then
        assertNotNull(resultat);
        verify(fileManager, never()).generatePresignedUrl(anyString());
    }

    @Test
    void testSauvegarder_DevraitModifierNomPhotoProfilAvecTimestamp() {
        // Given
        ImageUtilisateurModele imageModele = ImageUtilisateurModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("photo_original.jpg")
                .statut(StatutImage.PENDING)
                .build();

        ImageUtilisateurJpa imageJpa = ImageUtilisateurJpa.builder()
                .uuid(imageModele.getUuid())
                .urlImage("photo_original.jpg")
                .statut(StatutImage.PENDING)
                .build();

        UtilisateurModele utilisateurAvecPhoto = UtilisateurModele.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .photoProfil(imageModele)
                .build();

        UtilisateurJpa utilisateurJpaAvecPhoto = UtilisateurJpa.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .photoProfil(imageJpa)
                .build();

        when(mapper.versEntite(utilisateurAvecPhoto)).thenReturn(utilisateurJpaAvecPhoto);
        when(jpaRepository.save(any(UtilisateurJpa.class))).thenReturn(utilisateurJpaAvecPhoto);
        when(mapper.versModele(utilisateurJpaAvecPhoto)).thenReturn(utilisateurAvecPhoto);
        when(fileManager.generatePresignedUrl(anyString())).thenReturn("https://presigned-url.com/photo");

        // When
        UtilisateurModele resultat = providerAdapter.sauvegarder(utilisateurAvecPhoto);

        // Then
        assertNotNull(resultat);
        // Vérifier que save est appelé (le nom de l'image est modifié avec timestamp dans setPhotoProfilUnique)
        verify(jpaRepository, times(1)).save(any(UtilisateurJpa.class));
        verify(fileManager, times(1)).generatePresignedUrl(anyString());
    }
}
