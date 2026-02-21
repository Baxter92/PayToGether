package com.ulr.paytogether.wsclient.client.apiclient;

import com.ulr.paytogether.wsclient.dto.LoginRequest;
import com.ulr.paytogether.wsclient.dto.LoginResponse;
import com.ulr.paytogether.wsclient.service.AuthKeycloackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthApiCLient {

    @Value("${api.auth.url}")
    private String authApiUrl;
    @Value("${api.auth.admin.user}")
    private String adminUtilisateur;

    @Value("${api.auth.admin.password}")
    private String adminMotDePasse;

    private final AuthKeycloackService authKeycloackService;

    public LoginResponse getToken(String username, String password) {
        try {
            LoginRequest loginRequest = LoginRequest.builder()
                    .username(username)
                    .password(password)
                    .build();
            return (LoginResponse) authKeycloackService.post(authApiUrl, loginRequest);
        } catch (Exception e) {
            log.error("Erreur lors de l'obtention du token : {}", e.getMessage());
            throw new RuntimeException("Impossible d'obtenir le token d'authentification", e);
        }
    }

    public LoginResponse loginAdmin(){
        return this.getToken(adminUtilisateur, adminMotDePasse);
    }

    public String getValidToken(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", "Bearer " + token);
        return authKeycloackService.get(authApiUrl+"/api/auth/validate", headers).toString();
    }

    public Void logout(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", "Bearer " + token);
        return (Void) authKeycloackService.post(authApiUrl+"/api/auth/logout", null, headers);
    }
}
