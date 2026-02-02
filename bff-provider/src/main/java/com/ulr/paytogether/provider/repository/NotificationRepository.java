package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.core.domaine.entite.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité Notification
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Recherche toutes les notifications d'un utilisateur
     * @param utilisateurUuid l'UUID de l'utilisateur
     * @return la liste des notifications
     */
    List<Notification> findByUtilisateurUuid(UUID utilisateurUuid);

    /**
     * Recherche toutes les notifications non lues d'un utilisateur
     * @param utilisateurUuid l'UUID de l'utilisateur
     * @param lue false pour les notifications non lues
     * @return la liste des notifications non lues
     */
    List<Notification> findByUtilisateurUuidAndLue(UUID utilisateurUuid, Boolean lue);
}
