package com.ulr.paytogether.wsclient.client.apiclient;

import com.ulr.paytogether.wsclient.dto.UserRequest;
import com.ulr.paytogether.wsclient.dto.UserResponse;
import com.ulr.paytogether.wsclient.service.UserKeycloackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
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
            Map<String, Object> response = (Map<String, Object>) userKeycloackService.getWithAuth(authApiUrl+"/api/users", token);
            if (response.containsKey("data")) {
                List<Map<String, Object>> responseDatas =  (List<Map<String, Object>>) response.get("data");
                return responseDatas.stream().map(data -> UserResponse.builder()
                        .id((String) data.get("id"))
                        .username((String) data.get("username"))
                        .email((String) data.get("email"))
                        .enabled((Boolean) data.get("enabled"))
                        .firstName((String) data.get("firstName"))
                        .lastName((String) data.get("lastName"))
                        .build()).toList();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs : {}", e.getMessage());
            throw new RuntimeException("Impossible de récupérer les utilisateurs", e);
        }
        return Collections.emptyList();
    }

    public UserResponse getUser(String token, String userId) {
        try {
            Map<String, Object> response = (Map<String, Object>) userKeycloackService.getWithAuth(authApiUrl+"/api/users/"+userId, token);
            if (response.containsKey("data")) {
                Map<String, Object> data =  (Map<String, Object>) response.get("data");
                return UserResponse.builder()
                        .id((String) data.get("id"))
                        .username((String) data.get("username"))
                        .email((String) data.get("email"))
                        .firstName((String) data.get("firstName"))
                        .lastName((String) data.get("lastName"))
                        .enabled((Boolean) data.get("enabled"))
                        .build();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'utilisateur : {}", e.getMessage());
            throw new RuntimeException("Impossible de récupérer l'utilisateur", e);
        }
        return UserResponse.builder().build();
    }

    public UserResponse createUser(String token, UserRequest user) {
        try {
            Map<String, Object> response = (Map<String, Object>) userKeycloackService.postWithAuth(authApiUrl+"/api/users", user, token);
             if (response.containsKey("data")) {
                Map<String, Object> data =  (Map<String, Object>) response.get("data");
                return UserResponse.builder()
                        .id((String) data.get("id"))
                        .username((String) data.get("username"))
                        .email((String) data.get("email"))
                        .firstName((String) data.get("firstName"))
                        .lastName((String) data.get("lastName"))
                        .enabled((Boolean) data.get("enabled"))
                        .build();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'utilisateur : {}", e.getMessage());
            throw new RuntimeException("Impossible de créer l'utilisateur", e);
        }
        return UserResponse.builder().build();
    }

    public void assignRoleToUser(String token, String userId, String roleName) {
        try {
            userKeycloackService.postVoidWithAuth(authApiUrl+"/api/users/"+userId+"/roles/"+roleName, null, token);
        } catch (Exception e) {
            log.error("Erreur lors de l'assignation du rôle à l'utilisateur : {}", e.getMessage());
            throw new RuntimeException("Impossible d'assigner le rôle à l'utilisateur", e);
        }
    }

    public UserResponse updateUser(String token, String userId, UserRequest user) {
        try {
            Map<String, Object> response = (Map<String, Object>) userKeycloackService.putWithAuth(authApiUrl+"/api/users/"+userId, user, token);
             if (response.containsKey("data")) {
                Map<String, Object> data =  (Map<String, Object>) response.get("data");
                return UserResponse.builder()
                        .id((String) data.get("id"))
                        .username((String) data.get("username"))
                        .email((String) data.get("email"))
                        .firstName((String) data.get("firstName"))
                        .lastName((String) data.get("lastName"))
                        .enabled((Boolean) data.get("enabled"))
                        .build();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de l'utilisateur : {}", e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour l'utilisateur", e);
        }
        return UserResponse.builder().build();
    }

    public void resetPassword(String token, String userId, String newPassword) {
        try {
            Map<String, Object> passwordRequest = new HashMap<>();
            passwordRequest.put("newPassword", newPassword);
            passwordRequest.put("temporary", false);

            userKeycloackService.putVoidWithAuth(authApiUrl+"/api/users/"+userId+"/reset-password", passwordRequest, token);
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation du mot de passe : {}", e.getMessage());
            throw new RuntimeException("Impossible de réinitialiser le mot de passe", e);
        }
    }

    public void enableUser(String token, String userId, boolean enabled) {
        try {
            var enableEndpoint = enabled ? "/enable" : "/disable";
            userKeycloackService.putVoidWithAuth(authApiUrl+"/api/users/"+userId+enableEndpoint, null, token);
        } catch (Exception e) {
            log.error("Erreur lors de l'activation/désactivation de l'utilisateur : {}", e.getMessage());
            throw new RuntimeException("Impossible de modifier l'état de l'utilisateur", e);
        }
    }

    public void deleteUser(String token, String userId) {
        try {
            userKeycloackService.deleteVoidWithAuth(authApiUrl+"/api/users/"+userId, token);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'utilisateur : {}", e.getMessage());
            throw new RuntimeException("Impossible de supprimer l'utilisateur", e);
        }
    }
}
