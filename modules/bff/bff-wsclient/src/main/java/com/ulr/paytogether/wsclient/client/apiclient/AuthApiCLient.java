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

            Map<String, Object> response = (Map<String, Object>) authKeycloackService.post(authApiUrl+"/api/auth/login", loginRequest);
            if (response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                return LoginResponse.builder()
                        .accessToken((String) data.get("accessToken"))
                        .refreshToken((String) data.get("refreshToken"))
                        .tokenType((String) data.get("tokenType"))
                        .expiresIn((Integer) data.get("expiresIn"))
                        .build();
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'obtention du token : {}", e.getMessage());
            throw new RuntimeException("Impossible d'obtenir le token d'authentification", e);
        }
        return LoginResponse.builder().build();
    }

    public LoginResponse loginAdmin(){
        return this.getToken(adminUtilisateur, adminMotDePasse);
    }

    public String checkToken(String token) {
        Map<String, Object> response = (Map<String, Object>) authKeycloackService.getWithAuth(authApiUrl+"/api/auth/validate", token);
        return response.containsKey("data") ? (String) response.get("data") : null;
    }

    public void logout(String token) {
        authKeycloackService.postVoidWithAuth(authApiUrl+"/api/auth/logout", null, token);
    }
}
