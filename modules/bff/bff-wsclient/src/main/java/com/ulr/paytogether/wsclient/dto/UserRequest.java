package com.ulr.paytogether.wsclient.dto;

import lombok.Builder;
/**
 * DTO pour créer un utilisateur
 */
@Builder
public class UserRequest {
    
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
}
