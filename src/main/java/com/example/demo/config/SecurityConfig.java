package com.example.demo.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.beans.factory.annotation.Value;

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
    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth

                        // public endpoints
                        .requestMatchers("/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reports").permitAll()
                        .requestMatchers("/api/route/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/ws").permitAll()
                        .requestMatchers("/topic/**").permitAll()
                        .requestMatchers("/raw-test").permitAll()

                        // FIXED: Use hasAnyAuthority with exact prefixed role names
                        .requestMatchers("/api/employees/**").hasAnyAuthority("ROLE_admin-role", "ROLE_employee-role")
                        .requestMatchers("/api/vehicules/**").hasAnyAuthority("ROLE_admin-role", "ROLE_employee-role")
                        .requestMatchers("/api/containers/**").hasAnyAuthority("ROLE_admin-role", "ROLE_employee-role")

                        .requestMatchers("/api/tasks/**").hasAnyAuthority("ROLE_admin-role", "ROLE_employee-role")
                        .requestMatchers("/api/reports/**").hasAnyAuthority("ROLE_admin-role")
                        // everything else  → requires login
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
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


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