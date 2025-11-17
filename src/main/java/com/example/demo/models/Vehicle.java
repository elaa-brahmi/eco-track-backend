package com.example.demo.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("vehicles")
public class Vehicle {

    @Id
    private String id;

    private String name;
    private double[] location;
    private double capacity;
}