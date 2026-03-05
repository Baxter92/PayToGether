package com.ulr.paytogether.core.provider;

import java.util.Map;

/**
 * Interface Provider pour l'envoi d'emails (Port de sortie)
 * Cette interface définit le contrat pour l'envoi d'emails
 */
public interface EmailProvider {

    /**
     * Envoie un email avec un template HTML
     *
     * @param destinataire Email du destinataire
     * @param sujet Sujet de l'email
     * @param templateName Nom du template (sans préfixe ni extension)
     * @param variables Variables pour le template
     */
    void envoyerEmail(String destinataire, String sujet, String templateName, Map<String, Object> variables);
}

