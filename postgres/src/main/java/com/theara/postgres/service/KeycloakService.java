package com.theara.postgres.service;

import com.theara.postgres.model.BaseResponse;
import com.theara.postgres.model.request.LoginRequest;
import com.theara.postgres.model.request.UserRequest;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface KeycloakService {
    /**
     * Fetches an access token from the Keycloak server.
     *
     * @return The access token as a String.
     */
    //String getKeycloakToken();

    UserRepresentation getKeycloakUser(String username, String password);

    void updateKeycloakUser(String userId, Map<String, Object> userUpdates);

    void updateKeycloakPassword(String userId, String newPassword);

    String getToken(String username, String password);


    Map<String, Object> loginKeycloakUser(String username, String password);

    ResponseEntity<BaseResponse<String>> createKeycloakUser(UserRequest userRequest);

}
