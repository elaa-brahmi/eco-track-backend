package com.example.demo.models;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    @Id
    private String id;
    private String taskId;
    private List<String> containersIds;
    private String vehicleId;
    private List<String> routeOrder; // ["c8", "c3", "c12"]
    private String polyline; // encoded polyline
    private double totalDistanceKm;
    private double totalDurationMin;
    private Instant calculatedAt ;

}