package com.theara.postgres.config.keycloak;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class KeycloakJwtUtil {

    @Value("${keycloak.auth-server-url}")
    private  String serverUrl;

    @Value("${keycloak.realm}")
    private  String realm;

    private String getJwksUrl() {
        return serverUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
    }

    public PublicKey getPublicKey() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(getJwksUrl(), Map.class);
            List<Map<String, Object>> keys = (List<Map<String, Object>>) response.get("keys");

            for (Map<String, Object> key : keys) {
                if ("RS256".equals(key.get("alg"))) {
                    String modulus = (String) key.get("n");
                    String exponent = (String) key.get("e");
                    byte[] modulusBytes = Base64.getUrlDecoder().decode(modulus);
                    byte[] exponentBytes = Base64.getUrlDecoder().decode(exponent);

                    RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(1, modulusBytes), new BigInteger(1, exponentBytes));
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    return keyFactory.generatePublic(keySpec);
                }
            }
            throw new RuntimeException("Public key not found in JWKS");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching Keycloak public key", e);
        }
    }

    /**
     * Extract claims from the JWT.
     *
     * @param token the JWT token.
     * @return Claims object containing token details.
     */
    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getPublicKey()) // Use the public key for signature verification
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error extracting claims from the token", e);
        }
    }


    /**
     * Validate the JWT token.
     *
     * @param token the JWT token.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            // Parse claims to verify the signature and validity of the token
            extractClaims(token);
            return true; // If no exception occurs, the token is valid
        } catch (SignatureException e) {
            System.out.println("Invalid JWT signature: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getMessage());
            return false;
        }
    }
}
