package com.example.demo.models;


import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Document("employees")
public class Employee{
    @Id
    private String id;
    @Indexed(unique = true)
    private String keycloakId; //keycloak sub
    private String name;
    private String email;
    private String password;
    private Instant createdAt;
    private Role role;
    private boolean available;
}
