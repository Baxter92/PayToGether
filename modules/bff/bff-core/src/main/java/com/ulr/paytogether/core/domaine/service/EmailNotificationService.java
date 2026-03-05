package com.ulr.paytogether.core.domaine.service;

import java.util.Map;

/**
 * Interface de service pour l'envoi de notifications par email
 * Couche métier sans dépendance technique
 */
public interface EmailNotificationService {

    /**
     * Envoie un email de notification
     *
     * @param destinataire Email du destinataire
     * @param sujet Sujet de l'email
     * @param templateName Nom du template
     * @param variables Variables pour le template
     */
    void envoyerNotification(String destinataire, String sujet, String templateName, Map<String, Object> variables);
}

