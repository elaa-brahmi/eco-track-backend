package com.example.demo.service.employee;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;  // ← CORRECT
import org.keycloak.representations.idm.RoleRepresentation;       // ← CORRECT
import org.keycloak.representations.idm.UserRepresentation;        // ← THIS ONE WAS WRONG BEFORE
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakAdminService {

    private final Keycloak keycloak;

    public String createEmployeeUser(String email, String name, String password) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(name);
        user.setEmailVerified(true);

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setTemporary(false);                 // ← permanent password
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(password);

        user.setCredentials(List.of(cred));       // ← NOW WORKS!

        Response response = keycloak.realm("springboot-test")
                .users()
                .create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatusInfo());
        }

        String location = response.getLocation().toString();
        String keycloakId = location.substring(location.lastIndexOf("/") + 1);

        // Assign employee-role
        RoleRepresentation role = keycloak.realm("springboot-test")
                .roles()
                .get("employee-role")
                .toRepresentation();

        keycloak.realm("springboot-test")
                .users()
                .get(keycloakId)
                .roles()
                .realmLevel()
                .add(List.of(role));

        return keycloakId;
    }
}