package com.example.demo.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("vehicles")
public class Vehicle {

    @Id
    private String id;
    private String name;
    private boolean available;
    private double[] location;
    private int capacity; //number max of containers
    private List<AssignmentSlot> schedule;
}