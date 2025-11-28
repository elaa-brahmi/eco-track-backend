package com.example.demo.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth

                        // PUBLIC — NO TOKEN NEEDED
                        .requestMatchers("/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reports").permitAll()
                        .requestMatchers("/api/route/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/ws").permitAll()
                        .requestMatchers("/topic/**").permitAll()

                        // ADMIN ONLY
                        .requestMatchers("/api/employees/**").hasRole("admin-role")
                        .requestMatchers("/api/vehicules/**").hasRole("admin-role")
                        .requestMatchers("/api/containers/**").hasRole("admin-role")

                        // TASKS
                        .requestMatchers("/api/tasks/**").hasAnyRole("admin-role","employee-role")


                        .requestMatchers("/api/reports/**").hasAnyRole("admin-role")


                        // EVERYTHING ELSE → requires login
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * EXACTLY MATCHES YOUR KEYCLOAK TOKEN (realm_access.roles)
     * Your roles: admin-role, employee-role, citizen-role → become ROLE_admin-role etc.
     */
    //Role Extraction
    //Reads realm_access.roles → ["citizen-role", "offline_access", ...]
    //Converts to authorities: ["ROLE_citizen-role", "ROLE_offline_access", ...]
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var authorities = new ArrayList<GrantedAuthority>();

            // MAIN: Realm roles (this is where your roles are!)
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                roles.forEach(role ->
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role))
                );
            }

            // Optional: client roles (you have "admin" in react-app client)
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                resourceAccess.forEach((client, claims) -> {
                    if (claims instanceof Map && ((Map<?, ?>) claims).containsKey("roles")) {
                        List<String> roles = (List<String>) ((Map<?, ?>) claims).get("roles");
                        roles.forEach(role ->
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                    }
                });
            }

            return authorities;
        });

        return converter;
    }
}