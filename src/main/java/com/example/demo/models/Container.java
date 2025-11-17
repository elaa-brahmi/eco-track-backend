package com.example.demo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "containers")
@Data
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
    private double[] location; // [lng, lat] - match Mongo's 2dsphere expectation

    //private List<Prediction> predictions; // optional
}
