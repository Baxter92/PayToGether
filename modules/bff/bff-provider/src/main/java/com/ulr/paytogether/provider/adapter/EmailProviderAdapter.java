package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.provider.EmailProvider;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Adaptateur pour l'envoi d'emails (implémentation du port EmailProvider)
 * Cette classe fait le pont entre le domaine métier et l'infrastructure technique
 * (JavaMailSender, Thymeleaf)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailProviderAdapter implements EmailProvider {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String expediteur;

    /**
     * Implémentation de l'envoi d'email (Port de sortie)
     * Utilise l'infrastructure technique (JavaMailSender, Thymeleaf)
     *
     * @param destinataire Email du destinataire
     * @param sujet Sujet de l'email
     * @param templateName Nom du template (sans préfixe ni extension)
     * @param variables Variables pour le template
     */
    @Override
    public void envoyerEmail(String destinataire, String sujet, String templateName, Map<String, Object> variables) {
        try {
            log.info("Provider - Envoi d'email à: {} - Sujet: {}", destinataire, sujet);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
            );

            helper.setFrom(expediteur);
            helper.setTo(destinataire);
            helper.setSubject(sujet);

            // Génération du contenu HTML depuis le template Thymeleaf
            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }

            String contenuHtml = templateEngine.process("notifications/" + templateName, context);
            helper.setText(contenuHtml, true);

            // Envoi de l'email via JavaMailSender
            mailSender.send(message);

            log.info("Provider - Email envoyé avec succès à: {}", destinataire);

        } catch (MessagingException e) {
            log.error("Provider - Erreur lors de l'envoi d'email à {}: {}", destinataire, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi d'email", e);
        } catch (Exception e) {
            log.error("Provider - Erreur lors de la génération du template pour {}: {}", destinataire, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du template", e);
        }
    }
}

