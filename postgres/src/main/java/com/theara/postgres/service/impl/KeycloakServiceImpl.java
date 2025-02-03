package com.theara.postgres.service.impl;

import com.theara.postgres.config.keycloak.KeycloakJwtUtil;
import com.theara.postgres.model.BaseResponse;
import com.theara.postgres.model.request.UserRequest;
import com.theara.postgres.service.KeycloakService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class KeycloakServiceImpl implements KeycloakService {

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.client-secret}")
    private String secretKey;

    @Value("${keycloak.client-id}")
    private String clientId;

    private RestTemplate restTemplate;

    @Autowired
    private KeycloakJwtUtil keycloakJwtUtil;

    public KeycloakServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public String getKeycloakAdminToken() {
        try {
            // Build the request body
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("grant_type", "password");
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", secretKey);
            requestBody.add("username", adminUsername);
            requestBody.add("password", adminPassword);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Create HTTP request entity
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Send POST request to Keycloak
            ResponseEntity<Map> response = restTemplate.postForEntity(serverUrl + "/realms/" + realm + "/protocol/openid-connect/token", requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                // Extract access token from the response
                Map<String, Object> responseBody = response.getBody();
                assert responseBody != null;
                return (String) responseBody.get("access_token");
            } else {
                throw new RuntimeException("Failed to fetch token: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching Keycloak token", e);
        }
    }

    @Override
    public UserRepresentation getKeycloakUser(String username, String password) {
        String keycloakUrl = serverUrl + "/admin/realms/" + realm + "/users?username=" + username;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getKeycloakAdminToken());
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<UserRepresentation[]> response = restTemplate.exchange(
                keycloakUrl,
                HttpMethod.GET,
                requestEntity,
                UserRepresentation[].class
        );

        if (response.getBody() != null && response.getBody().length > 0) {
            return response.getBody()[0];
        }
        return null;
    }

    @Override
    public void updateKeycloakUser(String userId, Map<String, Object> userUpdates) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }

        if (userUpdates == null || userUpdates.isEmpty()) {
            throw new IllegalArgumentException("User updates cannot be null or empty.");
        }

        String keycloakUrl = serverUrl + "/admin/realms/" + realm + "/users/" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getKeycloakAdminToken());
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(userUpdates, headers);
        // Make PUT request
        restTemplate.put(keycloakUrl, requestEntity);
    }


    @Override
    public void updateKeycloakPassword(String userId, String newPassword) {
        try {
            String keycloakUrl = serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";
            Map<String, Object> credentials = new HashMap<>();
            credentials.put("type", "password");
            credentials.put("value", newPassword);
            credentials.put("temporary", false);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(getKeycloakAdminToken());
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(credentials, headers);
            restTemplate.put(keycloakUrl, requestEntity, Void.class);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Keycloak user password with ID: " + userId, e);
        }
    }

    @Override
    public String getToken(String username, String password) {
        try {
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("grant_type", "password");
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", secretKey);
            requestBody.add("username", username);
            requestBody.add("password", password);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Create HTTP request entity
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Send POST request to Keycloak
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    serverUrl + "/realms/" + realm + "/protocol/openid-connect/token",
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // Extract access token from the response
                Map<String, Object> responseBody = response.getBody();
                assert responseBody != null;
                return (String) responseBody.get("access_token");
            } else {
                throw new RuntimeException("Failed to fetch token: " + response.getStatusCode());
            }
        } catch (Exception ex) {
            // Handle exceptions and return an error response
            throw new RuntimeException("Error fetching Keycloak token", ex);
        }
    }


    public Map<String, Object> loginKeycloakUser(String username, String password) {
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("grant_type", "password");
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", secretKey);
            requestBody.add("username", username);
            requestBody.add("password", password);
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Create HTTP request entity
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Send POST request to Keycloak
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    serverUrl + "/realms/" + realm + "/protocol/openid-connect/token",
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // Extract access token from the response
                Map responseBody = response.getBody();
                assert responseBody != null;
                return responseBody;
            } else {
                throw new RuntimeException("Failed to fetch token: " + response.getStatusCode());
            }
    }

    @Override
    public ResponseEntity<BaseResponse<String>> createKeycloakUser(UserRequest userRequest) {
        // Set up Keycloak user details
        Map<String, Object> keycloakUser = new HashMap<>();
        keycloakUser.put("username", userRequest.getUsername());
        keycloakUser.put("email", userRequest.getEmail());
        keycloakUser.put("enabled", true);

        // Add credentials
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", userRequest.getPassword());
        credentials.put("temporary", false); // Password is not temporary

        keycloakUser.put("credentials", Collections.singletonList(credentials));

        // Construct Keycloak API URL
        String keycloakUrl = serverUrl + "/admin/realms/" + realm + "/users";

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getKeycloakAdminToken()); // Use the admin token

        // Create HTTP request entity
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(keycloakUser, headers);

        // Send POST request to create user in Keycloak
        ResponseEntity<String> keycloakResponse = restTemplate.postForEntity(keycloakUrl, requestEntity, String.class);

        if (keycloakResponse.getStatusCode() == HttpStatus.CREATED) {
            return ResponseEntity.ok(
                    new BaseResponse<>(
                            HttpStatus.CREATED.value(),
                            "User created successfully in Keycloak",
                            keycloakResponse.getBody()
                    )
            );
        } else if (keycloakResponse.getStatusCode() == HttpStatus.CONFLICT) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new BaseResponse<>(
                            HttpStatus.CONFLICT.value(),
                            "User with the same username or email already exists",
                            null
                    ));
        } else {
            return ResponseEntity.status(keycloakResponse.getStatusCode())
                    .body(new BaseResponse<>(
                            keycloakResponse.getStatusCodeValue(),
                            "Unexpected error occurred while creating Keycloak user",
                            null
                    ));
        }
    }

    // Method to extract user ID (sub claim) from the token
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(keycloakJwtUtil.getPublicKey())  // Ensure you have the public key to validate the token
                .parseClaimsJws(token)
                .getBody();

        // Extract the 'sub' (subject) claim, which typically holds the user ID
        return claims.getSubject();
    }
}

