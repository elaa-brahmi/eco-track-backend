package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RouteSolution {
    private List<String> containerOrder;   // ordered container IDs
    private double totalDistanceKm;
    private double totalDurationMin;
    private String encodedPolyline;

}
