package com.theara.postgres.config;

import com.theara.postgres.model.response.User;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {

    private String secretKey = "12345"; // Replace with your actual secret key

    public String generateToken(String username, String userId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("id", userId) // Add userId as a claim
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours expiration
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractUserId(String token) {
        return (String) extractClaims(token).get("id"); // Retrieve user ID from claims
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public boolean validateToken(String token, User userDetails) {
        if (token == null || token.isEmpty()) {
            System.out.println("JWT token is missing or empty");
            return false; // Token is missing or empty
        }

        try {
            Claims claims = extractClaims(token);
            boolean isExpired = isTokenExpired(token);
            return !isExpired; /*&& claims.getSubject().equals(userDetails.getUsername());*/
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("Unsupported JWT token: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("Malformed JWT token: " + e.getMessage());
        } catch (SignatureException e) {
            System.out.println("Invalid JWT signature: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT token is missing or incorrect: " + e.getMessage());
        }

        return false; // If any exception occurs, the token is not valid
    }
}
