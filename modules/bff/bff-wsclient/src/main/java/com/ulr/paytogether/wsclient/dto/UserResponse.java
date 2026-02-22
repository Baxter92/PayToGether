package com.ulr.paytogether.wsclient.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO pour la réponse utilisateur
 */
@Data
@Builder
public class UserResponse {
    
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private boolean emailVerified;
    private Long createdTimestamp;
    private List<String> roles;
}
