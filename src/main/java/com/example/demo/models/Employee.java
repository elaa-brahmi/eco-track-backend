package com.example.demo.models;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("employees")
public class Employee extends User{

    @Id
    private String id;
    private String role;
    private boolean available;
}
