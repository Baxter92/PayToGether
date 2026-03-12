package com.ulr.paytogether.bff.event.utils;

import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.annotation.Value;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class EventUtils {
    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    /**
     * Construit le lien de validation avec le token
     */
    public static String CONSTRUIRELIENDEAL(String dealUuid) {
        return frontendBaseUrl + "/deals/%s".formatted(dealUuid);
    }
}
