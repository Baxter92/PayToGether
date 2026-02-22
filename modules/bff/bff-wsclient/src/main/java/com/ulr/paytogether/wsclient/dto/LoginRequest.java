package com.ulr.paytogether.wsclient.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO pour la requête de login
 */
@Builder
@Data
public class LoginRequest {
    
    private String username;
    private String password;
}
