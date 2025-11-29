package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RouteSolution {
    private List<String> containerOrder;   // ordered container IDs
    private double totalDistanceKm;
    private double totalDurationMin;
    private String encodedPolyline;

}
