package com.ulr.paytogether.bff.event.utils;

import lombok.experimental.UtilityClass;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class EventUtils {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    /**
     * Construit le lien de validation avec le token
     * @param dealUuid UUID du deal
     * @param frontendBaseUrl URL de base du frontend
     */
    public static String CONSTRUIRELIENDEAL(String dealUuid, String frontendBaseUrl) {
        return frontendBaseUrl + "/deals/%s".formatted(dealUuid);
    }
}
