package com.ulr.paytogether.wsclient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse de login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Integer expiresIn;
    private Integer refreshExpiresIn;
    private String scope;
}
