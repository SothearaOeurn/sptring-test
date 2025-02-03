package com.theara.postgres.config.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theara.postgres.config.JwtUtil;
import com.theara.postgres.model.BaseResponse;
import com.theara.postgres.service.impl.UserServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestKeycloakFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserServiceImpl userServiceImpl;

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestKeycloakFilter.class);

    @Value("${app.predefined.username}")
    private String predefinedUsername;

    @Value("${app.predefined.password}")
    private String predefinedPassword;

    private final KeycloakJwtUtil keycloakJwtUtil;

    public JwtRequestKeycloakFilter(KeycloakJwtUtil keycloakJwtUtil) {
        this.keycloakJwtUtil = keycloakJwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String token = null;
        String username = null;

        String requestUri = request.getRequestURI();
        // Check if the request URI is a public URL
        if (SecurityKeycloakConfig.isPublicUrl(requestUri)) {
            logger.debug("Public URL accessed: {}", requestUri);
            chain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");
        // Extract JWT from the Authorization header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            try {
                Claims claims = keycloakJwtUtil.extractClaims(token);
                username = claims.getSubject(); // Extract subject (username) from the token
            }
            catch (Exception e) {
                handleErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token has expired: " + e.getMessage());
                return; // Stop further processing
            }
        } else {
            handleErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization header is missing or invalid");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Normally, you'd load the user from a database here. For demo, create a dummy UserDetails object.
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(username)
                    .password("") // Password is not needed here
                    .authorities("USER") // Assign default roles (customize as needed)
                    .build();

            if (keycloakJwtUtil.validateToken(token)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                handleErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
            }
        }

        chain.doFilter(request, response);
    }

    // Helper method to handle error responses
    private void handleErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        logger.error("Handling error response: statusCode: {}, message: {}", statusCode, message);
        response.setStatus(statusCode);
        BaseResponse<String> errorResponse = new BaseResponse<>(statusCode, message, null);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}

