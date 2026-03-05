package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.ValidationTokenJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository JPA pour les tokens de validation
 */
@Repository
public interface ValidationTokenRepository extends JpaRepository<ValidationTokenJpa, UUID> {

    /**
     * Recherche un token par sa valeur
     *
     * @param token La valeur du token
     * @return Le token si trouvé
     */
    Optional<ValidationTokenJpa> findByToken(String token);

    /**
     * Vérifie si un token existe et est valide
     *
     * @param token La valeur du token
     * @param maintenant La date actuelle
     * @return true si le token existe et n'est pas expiré
     */
    boolean existsByTokenAndUtiliseFalseAndDateExpirationAfter(String token, LocalDateTime maintenant);

    /**
     * Supprime les tokens expirés
     *
     * @param maintenant La date actuelle
     */
    void deleteByDateExpirationBefore(LocalDateTime maintenant);

    /**
     * Supprime les tokens d'un utilisateur
     *
     * @param utilisateurUuid UUID de l'utilisateur
     */
    void deleteByUtilisateurUuid(UUID utilisateurUuid);
}

