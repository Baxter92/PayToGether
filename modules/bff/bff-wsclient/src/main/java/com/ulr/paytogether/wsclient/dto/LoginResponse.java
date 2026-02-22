package com.ulr.paytogether.wsclient.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO pour la réponse de login
 */
@Data
@Builder
public class LoginResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Integer expiresIn;
    private Integer refreshExpiresIn;
    private String scope;
}
