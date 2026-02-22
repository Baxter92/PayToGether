package com.ulr.paytogether.wsclient.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO pour créer un utilisateur
 */
@Builder
@Data
public class UserRequest {
    
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    @Builder.Default
    private boolean enabled = true;
}
