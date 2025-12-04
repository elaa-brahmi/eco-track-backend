package com.example.demo.dto;

import com.example.demo.models.TaskPriority;
import com.example.demo.models.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RouteWithTaskDto {
    private String routeId;
    private String taskId;
    private String vehicleId;
    private List<String> containersIds;
    private List<String> containerOrder;
    private String polyline;
    private double totalDistanceKm;
    private double totalDurationMin;
    private Instant calculatedAt;
}
