package com.example.demo.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "containers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Container {
    @Id
    private String id;
    private String type; // plastic, organic ...
    private int fillLevel; // 0-100
    private String status; // normal, overflowing, maintenance
    private Instant lastUpdated;

    // GeoJSON location
    private double[] location; //

    //private List<Prediction> predictions; // optional
}
