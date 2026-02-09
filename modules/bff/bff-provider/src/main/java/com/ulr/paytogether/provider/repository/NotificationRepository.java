package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.NotificationJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité Notification
 */
@Repository
public interface NotificationRepository extends JpaRepository<NotificationJpa, UUID> {

    /**
     * Recherche toutes les notifications d'un utilisateur
     * @param utilisateurJpa l'entité UtilisateurJpa de l'utilisateur
     * @return la liste des notifications
     */
    List<NotificationJpa> findByUtilisateurJpa(UtilisateurJpa utilisateurJpa);

    /**
     * Recherche toutes les notifications d'un utilisateur selon leur statut de lecture
     * @param utilisateurJpa l'entité UtilisateurJpa de l'utilisateur
     * @param lue true pour les notifications lues, false pour les notifications non lues
     * @return la liste des notifications selon leur statut de lecture
     */
    List<NotificationJpa> findByUtilisateurJpaAndLue(UtilisateurJpa utilisateurJpa, Boolean lue);
}
