package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.exception.ResourceNotFoundException;
import com.ulr.paytogether.core.modele.CommentaireModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.provider.adapter.entity.CommentaireJpa;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.CommentaireJpaMapper;
import com.ulr.paytogether.provider.repository.CommentaireRepository;
import com.ulr.paytogether.provider.repository.DealRepository;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CommentaireProviderAdapter
 */
@ExtendWith(MockitoExtension.class)
class CommentaireProviderAdapterTest {

    @Mock
    private CommentaireRepository jpaRepository;

    @Mock
    private DealRepository dealRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private CommentaireJpaMapper mapper;

    @InjectMocks
    private CommentaireProviderAdapter adapter;

    private CommentaireModele commentaireModele;
    private CommentaireJpa commentaireJpa;
    private DealJpa dealJpa;
    private UtilisateurJpa utilisateurJpa;
    private UUID commentaireUuid;
    private UUID dealUuid;
    private UUID utilisateurUuid;

    @BeforeEach
    void setUp() {
        commentaireUuid = UUID.randomUUID();
        dealUuid = UUID.randomUUID();
        utilisateurUuid = UUID.randomUUID();

        utilisateurJpa = UtilisateurJpa.builder()
                .uuid(utilisateurUuid)
                .build();

        dealJpa = DealJpa.builder()
                .uuid(dealUuid)
                .build();

        commentaireJpa = CommentaireJpa.builder()
                .uuid(commentaireUuid)
                .contenu("Excellent deal")
                .note(5)
                .utilisateurJpa(utilisateurJpa)
                .dealJpa(dealJpa)
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        commentaireModele = CommentaireModele.builder()
                .uuid(commentaireUuid)
                .contenu("Excellent deal")
                .note(5)
                .utilisateur(UtilisateurModele.builder().uuid(utilisateurUuid).build())
                .deal(DealModele.builder().uuid(dealUuid).build())
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();
    }

    @Test
    void sauvegarder_DevraitSauvegarderCommentaire() {
        // Given
        when(mapper.versEntite(commentaireModele)).thenReturn(commentaireJpa);
        when(jpaRepository.save(commentaireJpa)).thenReturn(commentaireJpa);
        when(mapper.versModele(commentaireJpa)).thenReturn(commentaireModele);

        // When
        CommentaireModele resultat = adapter.sauvegarder(commentaireModele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getUuid()).isEqualTo(commentaireUuid);

        verify(mapper).versEntite(commentaireModele);
        verify(jpaRepository).save(commentaireJpa);
        verify(mapper).versModele(commentaireJpa);
    }

    @Test
    void mettreAJour_DevraitMettreAJourCommentaire_QuandExiste() {
        // Given
        when(jpaRepository.findById(commentaireUuid)).thenReturn(Optional.of(commentaireJpa));
        when(jpaRepository.save(commentaireJpa)).thenReturn(commentaireJpa);
        when(mapper.versModele(commentaireJpa)).thenReturn(commentaireModele);

        // When
        CommentaireModele resultat = adapter.mettreAJour(commentaireUuid, commentaireModele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getUuid()).isEqualTo(commentaireUuid);

        verify(jpaRepository).findById(commentaireUuid);
        verify(mapper).mettreAJour(commentaireJpa, commentaireModele);
        verify(jpaRepository).save(commentaireJpa);
        verify(mapper).versModele(commentaireJpa);
    }

    @Test
    void mettreAJour_DevraitLeverResourceNotFoundException_QuandNonExiste() {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        when(jpaRepository.findById(uuidInexistant)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adapter.mettreAJour(uuidInexistant, commentaireModele))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("commentaire")
                .hasMessageContaining(uuidInexistant.toString());

        verify(jpaRepository).findById(uuidInexistant);
        verify(jpaRepository, never()).save(any());
    }

    @Test
    void trouverParUuid_DevraitRetournerCommentaire_QuandExiste() {
        // Given
        when(jpaRepository.findById(commentaireUuid)).thenReturn(Optional.of(commentaireJpa));
        when(mapper.versModele(commentaireJpa)).thenReturn(commentaireModele);

        // When
        Optional<CommentaireModele> resultat = adapter.trouverParUuid(commentaireUuid);

        // Then
        assertThat(resultat).isPresent();
        assertThat(resultat.get().getUuid()).isEqualTo(commentaireUuid);

        verify(jpaRepository).findById(commentaireUuid);
        verify(mapper).versModele(commentaireJpa);
    }

    @Test
    void trouverParUuid_DevraitRetournerEmpty_QuandNonExiste() {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        when(jpaRepository.findById(uuidInexistant)).thenReturn(Optional.empty());

        // When
        Optional<CommentaireModele> resultat = adapter.trouverParUuid(uuidInexistant);

        // Then
        assertThat(resultat).isEmpty();

        verify(jpaRepository).findById(uuidInexistant);
        verify(mapper, never()).versModele(any());
    }

    @Test
    void trouverParDeal_DevraitRetournerListeCommentaires() {
        // Given
        CommentaireJpa commentaire2 = CommentaireJpa.builder()
                .uuid(UUID.randomUUID())
                .contenu("Bon deal")
                .note(4)
                .build();

        List<CommentaireJpa> commentairesJpa = List.of(commentaireJpa, commentaire2);

        when(dealRepository.findById(dealUuid)).thenReturn(Optional.of(dealJpa));
        when(jpaRepository.findByDealJpa(dealJpa)).thenReturn(commentairesJpa);
        when(mapper.versModele(any(CommentaireJpa.class))).thenReturn(commentaireModele);

        // When
        List<CommentaireModele> resultat = adapter.trouverParDeal(dealUuid);

        // Then
        assertThat(resultat).hasSize(2);

        verify(dealRepository).findById(dealUuid);
        verify(jpaRepository).findByDealJpa(dealJpa);
        verify(mapper, times(2)).versModele(any(CommentaireJpa.class));
    }

    @Test
    void trouverParDeal_DevraitLeverResourceNotFoundException_QuandDealNonExiste() {
        // Given
        UUID dealInexistant = UUID.randomUUID();
        when(dealRepository.findById(dealInexistant)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adapter.trouverParDeal(dealInexistant))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("deal")
                .hasMessageContaining(dealInexistant.toString());

        verify(dealRepository).findById(dealInexistant);
        verify(jpaRepository, never()).findByDealJpa(any());
    }

    @Test
    void trouverParUtilisateur_DevraitRetournerListeCommentaires() {
        // Given
        List<CommentaireJpa> commentairesJpa = List.of(commentaireJpa);

        when(utilisateurRepository.findById(utilisateurUuid)).thenReturn(Optional.of(utilisateurJpa));
        when(jpaRepository.findByUtilisateurJpa(utilisateurJpa)).thenReturn(commentairesJpa);
        when(mapper.versModele(commentaireJpa)).thenReturn(commentaireModele);

        // When
        List<CommentaireModele> resultat = adapter.trouverParUtilisateur(utilisateurUuid);

        // Then
        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getUuid()).isEqualTo(commentaireUuid);

        verify(utilisateurRepository).findById(utilisateurUuid);
        verify(jpaRepository).findByUtilisateurJpa(utilisateurJpa);
        verify(mapper).versModele(commentaireJpa);
    }

    @Test
    void trouverParUtilisateur_DevraitLeverResourceNotFoundException_QuandUtilisateurNonExiste() {
        // Given
        UUID utilisateurInexistant = UUID.randomUUID();
        when(utilisateurRepository.findById(utilisateurInexistant)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adapter.trouverParUtilisateur(utilisateurInexistant))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("utilisateur")
                .hasMessageContaining(utilisateurInexistant.toString());

        verify(utilisateurRepository).findById(utilisateurInexistant);
        verify(jpaRepository, never()).findByUtilisateurJpa(any());
    }

    @Test
    void trouverTous_DevraitRetournerTousLesCommentaires() {
        // Given
        List<CommentaireJpa> commentairesJpa = List.of(commentaireJpa);

        when(jpaRepository.findAll()).thenReturn(commentairesJpa);
        when(mapper.versModele(commentaireJpa)).thenReturn(commentaireModele);

        // When
        List<CommentaireModele> resultat = adapter.trouverTous();

        // Then
        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getUuid()).isEqualTo(commentaireUuid);

        verify(jpaRepository).findAll();
        verify(mapper).versModele(commentaireJpa);
    }

    @Test
    void supprimerParUuid_DevraitSupprimerCommentaire() {
        // Given
        doNothing().when(jpaRepository).deleteById(commentaireUuid);

        // When
        adapter.supprimerParUuid(commentaireUuid);

        // Then
        verify(jpaRepository).deleteById(commentaireUuid);
    }
}

