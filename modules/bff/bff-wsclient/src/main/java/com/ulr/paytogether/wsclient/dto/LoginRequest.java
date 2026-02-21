package com.ulr.paytogether.wsclient.dto;

import lombok.Builder;

/**
 * DTO pour la requête de login
 */
@Builder
public class LoginRequest {
    
    private String username;
    private String password;
}
