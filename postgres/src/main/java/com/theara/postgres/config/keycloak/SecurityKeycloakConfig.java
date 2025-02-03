package com.theara.postgres.config.keycloak;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityKeycloakConfig {

    private final JwtRequestKeycloakFilter jwtRequestFilter;

    private final KeycloakJwtUtil keycloakJwtUtil;

    private static final List<String> PUBLIC_URLS = Arrays.asList(
            "/api/users/login",
            "/api/users/register",
            //"/api/users/update",
            "/public-endpoint"
            // Add more public endpoints here
    );

    public SecurityKeycloakConfig(JwtRequestKeycloakFilter jwtRequestFilter,KeycloakJwtUtil keycloakJwtUtil) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.keycloakJwtUtil = keycloakJwtUtil;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)// Disable CSRF protection
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(PUBLIC_URLS.toArray(new String[0])).permitAll() // Allow public access to registration
                        .requestMatchers("/api/users/**").authenticated() // Require authentication for other endpoints
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Use stateless sessions
                );
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Method to check if a URL is public
    public static boolean isPublicUrl(String path) {
        return PUBLIC_URLS.contains(path);
    }


}
