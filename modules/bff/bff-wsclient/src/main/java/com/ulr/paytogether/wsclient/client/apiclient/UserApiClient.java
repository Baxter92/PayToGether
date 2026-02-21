package com.ulr.paytogether.wsclient.client.apiclient;

import com.ulr.paytogether.wsclient.dto.UserRequest;
import com.ulr.paytogether.wsclient.dto.UserResponse;
import com.ulr.paytogether.wsclient.service.UserKeycloackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserApiClient {
    @Value("${api.auth.url}")
    private String authApiUrl;
    private final UserKeycloackService userKeycloackService;

    public List<UserResponse> getAllUsers(String token) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authentication", "Bearer " + token);
            return (List<UserResponse>) userKeycloackService.get(authApiUrl+"/api/users", headers);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs : {}", e.getMessage());
            throw new RuntimeException("Impossible de récupérer les utilisateurs", e);
        }
    }

    public UserResponse getUser(String token, String userId) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authentication", "Bearer " + token);
            return (UserResponse) userKeycloackService.get(authApiUrl+"/api/users/"+userId, headers);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'utilisateur : {}", e.getMessage());
            throw new RuntimeException("Impossible de récupérer l'utilisateur", e);
        }
    }

    public UserResponse createUser(String token, UserRequest user) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authentication", "Bearer " + token);

            return (UserResponse) userKeycloackService.post(authApiUrl+"/api/users", user, headers);
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'utilisateur : {}", e.getMessage());
            throw new RuntimeException("Impossible de créer l'utilisateur", e);
        }
    }

    public void assignRoleToUser(String token, String userId, String roleName) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authentication", "Bearer " + token);
            userKeycloackService.post(authApiUrl+"/api/users/"+userId+"/roles/"+roleName, null, headers);
        } catch (Exception e) {
            log.error("Erreur lors de l'assignation du rôle à l'utilisateur : {}", e.getMessage());
            throw new RuntimeException("Impossible d'assigner le rôle à l'utilisateur", e);
        }
    }

    public UserResponse updateUser(String token, String userId, UserRequest user) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authentication", "Bearer " + token);
            return (UserResponse) userKeycloackService.put(authApiUrl+"/api/users/"+userId, user, headers);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de l'utilisateur : {}", e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour l'utilisateur", e);
        }
    }

    public void resetPassword(String token, String userId, String newPassword) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authentication", "Bearer " + token);

            Map<String, Object> passwordRequest = new HashMap<>();
            passwordRequest.put("password", newPassword);
            passwordRequest.put("temporary", false);

            userKeycloackService.put(authApiUrl+"/api/users/"+userId+"/reset-password", passwordRequest, headers);
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation du mot de passe : {}", e.getMessage());
            throw new RuntimeException("Impossible de réinitialiser le mot de passe", e);
        }
    }

    public void enableUser(String token, String userId, boolean enabled) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authentication", "Bearer " + token);
            var enableEndpoint = enabled ? "/enable" : "/disable";
            userKeycloackService.put(authApiUrl+"/api/users/"+userId+enableEndpoint, null, headers);
        } catch (Exception e) {
            log.error("Erreur lors de l'activation/désactivation de l'utilisateur : {}", e.getMessage());
            throw new RuntimeException("Impossible de modifier l'état de l'utilisateur", e);
        }
    }

    public void deleteUser(String token, String userId) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authentication", "Bearer " + token);
            userKeycloackService.delete(authApiUrl+"/api/users/"+userId, headers);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'utilisateur : {}", e.getMessage());
            throw new RuntimeException("Impossible de supprimer l'utilisateur", e);
        }
    }
}
