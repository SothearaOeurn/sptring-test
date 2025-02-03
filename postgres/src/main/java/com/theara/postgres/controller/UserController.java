package com.theara.postgres.controller;

import com.theara.postgres.config.JwtUtil;
import com.theara.postgres.model.BaseResponse;
import com.theara.postgres.model.request.LoginRequest;
import com.theara.postgres.model.request.UpdateRequest;
import com.theara.postgres.model.request.UserRequest;
import com.theara.postgres.model.response.User;
import com.theara.postgres.service.impl.UserServiceImpl;
import com.theara.postgres.service.impl.KeycloakServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.keycloak.admin.client.Keycloak;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
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

    private Keycloak keycloak;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KeycloakServiceImpl keycloakServiceImpl;

    @PostConstruct
    public void init() {
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .username(adminUsername)
                .password(adminPassword)
                .clientSecret(secretKey)
                .build();
    }

    @GetMapping("all")
    public ResponseEntity<BaseResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = userServiceImpl.getAllUsers();
            return ResponseEntity.ok()
                    .body(new BaseResponse<>(HttpStatus.OK.value(), "Users retrieved successfully", users));
        } catch (Exception e) {
            log.error("Failed to retrieve users", e); // Log error message with exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred", null));
        }
    }

    @GetMapping
    public ResponseEntity<BaseResponse<User>> getUserById() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String tokenUserId = jwtUtil.extractUserId(authentication.getCredentials().toString());
            Long userId = Long.valueOf(tokenUserId);

            Optional<User> user = userServiceImpl.getUserId(userId);
            return user.map(value -> ResponseEntity.ok()
                            .body(new BaseResponse<>(HttpStatus.OK.value(), "User retrieved successfully", value)))
                    .orElseGet(() -> {
                        log.warn("User with ID {} not found", userId); // Log warning if user not found
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new BaseResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null));
                    });
        } catch (Exception e) {
            log.error("Error retrieving user by ID", e); // Log error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred", null));
        }
    }

    @GetMapping("/user-info")
    public ResponseEntity<BaseResponse<User>> getUserByUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String tokenUserId = jwtUtil.extractUserId(authentication.getCredentials().toString());
            Optional<User> user = userServiceImpl.getUserByUserId(tokenUserId);
            return user.map(value -> ResponseEntity.ok()
                            .body(new BaseResponse<>(HttpStatus.OK.value(), "User retrieved successfully", value)))
                    .orElseGet(() -> {
                        log.warn("User with ID {} not found", tokenUserId); // Log warning if user not found
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new BaseResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null));
                    });
        } catch (Exception e) {
            log.error("Error retrieving user by ID", e); // Log error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred", null));
        }
    }

    @PutMapping
    public ResponseEntity<BaseResponse<User>> updateUser(@RequestBody UserRequest userRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String tokenUserId = jwtUtil.extractUserId(authentication.getCredentials().toString());

            Optional<User> user = userServiceImpl.getUserId(Long.valueOf(tokenUserId));
            if (!user.isPresent()) {
                log.warn("User with ID {} not found", tokenUserId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new BaseResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null));
            }

            User existingUser = user.get();
            existingUser.setUsername(userRequest.getUsername() != null ? userRequest.getUsername() : existingUser.getUsername());
            existingUser.setEmail(userRequest.getEmail() != null ? userRequest.getEmail() : existingUser.getEmail());
            existingUser.setPassword(userRequest.getPassword() != null ? passwordEncoder.encode(userRequest.getPassword()) : existingUser.getPassword());
            existingUser.setAccessToken(tokenUserId);
            User updatedUser = userServiceImpl.saveUser(existingUser);

            return ResponseEntity.ok()
                    .body(new BaseResponse<>(HttpStatus.OK.value(), "User updated successfully", updatedUser));
        } catch (Exception e) {
            log.error("Error updating user", e); // Log error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while updating user", null));
        }
    }

    @DeleteMapping
    public ResponseEntity<BaseResponse<Void>> deleteUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String tokenUserId = jwtUtil.extractUserId(authentication.getCredentials().toString());
            Long userId = Long.valueOf(tokenUserId);

            if (userServiceImpl.getUserId(userId).isPresent()) {
                userServiceImpl.deleteUser(userId);
                log.info("User with ID {} deleted successfully", userId); // Log success message
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new BaseResponse<>(HttpStatus.OK.value(), "User deleted successfully", null));
            } else {
                log.warn("User with ID {} not found for deletion", userId); // Log warning
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new BaseResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null));
            }
        } catch (Exception e) {
            log.error("Error deleting user", e); // Log error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while deleting the user", null));
        }
    }
    //@CrossOrigin("10.0.2.16")
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<Map>> loginUser(@RequestBody LoginRequest userRequest) {
        try {
            Map<String, Object> userLogin = keycloakServiceImpl.loginKeycloakUser(userRequest.getUsername(), userRequest.getPassword());
            return ResponseEntity.ok(
                    new BaseResponse<>(
                            HttpStatus.OK.value(),
                            "Login successful",
                            userLogin
                    )
            );
        } catch (HttpClientErrorException e) {
            log.error("Error message", e);
            throw e;
        } catch (Exception e) {
            log.error("Error message", e);
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<User>> registerUser(@RequestBody UserRequest userRequest) throws Exception {
        try {
            User user = new User();
            keycloakServiceImpl.createKeycloakUser(userRequest);
            UserRepresentation userRepresentation = keycloakServiceImpl.getKeycloakUser(userRequest.getUsername(), userRequest.getPassword());
            if (userRepresentation != null) {
                Map<String, Object> userResponse = keycloakServiceImpl.loginKeycloakUser(userRequest.getUsername(), userRequest.getPassword());
                if (!userResponse.isEmpty()) {
                    String accessToken = (String) userResponse.get("access_token");
                    String refreshToken = (String) userResponse.get("refresh_token");
                    Integer expireIn = (Integer) userResponse.get("expires_in");
                    Integer refresh_expires_in = (Integer) userResponse.get("refresh_expires_in");
                    user.setUserId(userRepresentation.getId());
                    user.setUsername(userRepresentation.getUsername());
                    user.setEmail(userRepresentation.getEmail());
                    user.setExpiresIn(expireIn);
                    user.setRefreshExpiresIn(refresh_expires_in);
                    try {
                        userServiceImpl.saveUser(user);
                        user.setAccessToken(accessToken);
                        user.setRefreshToken(refreshToken);
                    } catch (Exception e) {
                        log.error("Error saving user", e);
                        throw e;
                    }
                }
            }

            return ResponseEntity.ok()
                    .body(new BaseResponse<>(HttpStatus.CREATED.value(), "User registered successfully", user));
        } catch (HttpClientErrorException e) {
            log.error("Error message", e);
            throw e;
        }
    }

    @PutMapping("/update")
    public ResponseEntity<BaseResponse<User>> update(@RequestBody UpdateRequest userRequest, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Extract the token from the Authorization header
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            // Extract user ID from the JWT token
            String userId = keycloakServiceImpl.getUserIdFromToken(token);
            Map<String, Object> userUpdates = Map.of(
                    "email", userRequest.getEmail()
            );
            // Update the Keycloak user's details
            keycloakServiceImpl.updateKeycloakUser(userId, userUpdates);
            return ResponseEntity.ok()
                    .body(new BaseResponse<>(HttpStatus.OK.value(), "User updated successfully", null));

        } catch (HttpClientErrorException e) {
            // Log the exception (you can customize this further)
            log.error("Error message", e);
            throw new RuntimeException("Error updating user", e);
        }
    }
}

