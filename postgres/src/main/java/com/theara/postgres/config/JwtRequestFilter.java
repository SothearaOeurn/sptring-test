/*
package com.theara.postgres.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theara.postgres.model.BaseResponse;
import com.theara.postgres.model.response.User;
import com.theara.postgres.service.impl.UserServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserServiceImpl userServiceImpl;

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Value("${app.predefined.username}")
    private String predefinedUsername;

    @Value("${app.predefined.password}")
    private String predefinedPassword;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String requestUri = request.getRequestURI();
        logger.info("Incoming request: URI: {}", requestUri);
        // Get the requested URI
        String requestUri = request.getRequestURI();
        logger.info("Incoming request: URI: {}", requestUri);

        // Check for Basic Authentication
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String[] userPass = credentials.split(":", 2);

            // Validate username and password
            if (userPass.length == 2 && predefinedUsername.equals(userPass[0]) && predefinedPassword.equals(userPass[1])) {
                logger.info("Basic Authentication successful for user: {}", predefinedUsername);
                // Optionally, you could create a valid authentication token here if needed.
            } else {
                logger.warn("Basic Authentication failed for user: {}", userPass[0]);
                handleErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization is invalid or incorrect");
                return;
            }
        } else {
            logger.warn("Authorization header is missing or does not start with Basic for URI: {}", requestUri);
            handleErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization header is missing or invalid");
            return;
        }

        // Check if the request URI is a public URL
        if (SecurityConfig.isPublicUrl(requestUri)) {
            logger.debug("Public URL accessed: {}", requestUri);
            chain.doFilter(request, response);
            return;
        }

        // Extract the JWT token from the custom header "xtoken"
        final String authorizationHeader = request.getHeader("xtoken");
        logger.debug("Authorization header extracted: {}", authorizationHeader != null ? "[REDACTED]" : "null");

        String userId = null;
        String jwtToken = null;

        // Check if the authorization header is present and not empty
        if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
            jwtToken = authorizationHeader;
            try {
                // Extract userId from the JWT token
                userId = jwtUtil.extractUserId(jwtToken);
                logger.info("JWT token valid. Extracted userId: {}", userId);
            } catch (Exception e) {
                logger.error("Error parsing JWT token: {}", e.getMessage());
                handleErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                return;
            }
        } else {
            logger.warn("Authorization header is missing or empty for URI: {}", requestUri);
            handleErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization header is missing or empty");
            return;
        }

        // Validate the token and authenticate the user if the token is valid
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Checking user details for userId: {}", userId);
            User user = this.userService.getUserById(Long.valueOf(userId)).orElse(null);

            if (user == null) {
                logger.warn("User not found for userId: {}", userId);
                handleErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            logger.debug("Loading user details for userId: {}", userId);
            UserDetail userDetail = this.userDetailsService.loadUserById(Long.valueOf(userId));

            if (jwtUtil.validateToken(jwtToken, user)) {
                logger.info("JWT token validated successfully for userId: {}", userId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, jwtToken, userDetail.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.warn("JWT token validation failed for userId: {}", userId);
                handleErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
                return;
            }
        }

        //logger.info("Authentication successful. Proceeding with request for userId: {}", userId);
        chain.doFilter(request, response);
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

*/
