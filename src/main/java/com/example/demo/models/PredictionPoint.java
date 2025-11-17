package com.example.demo.models;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionPoint {
    private Instant timestamp;
    private double predictedFill;
}