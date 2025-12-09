package com.example.demo.controller;

import com.example.demo.dto.OptimizeRequest;
import com.example.demo.dto.RouteSolution;
import com.example.demo.dto.RouteWithTaskDto;
import com.example.demo.models.Container;
import com.example.demo.models.Route;
import com.example.demo.models.Vehicle;
import com.example.demo.service.routing.RouteOptimizationService;
import com.example.demo.service.routing.RouteService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class RouteController {
    private final RouteService routeService;
    @GetMapping()
    public ResponseEntity<List<Route>> getRoutes() {
        return ResponseEntity.status(200).body(routeService.getAllRoutes());

    }
    @GetMapping("/activeRoutes")
    public ResponseEntity<List<RouteWithTaskDto>> getActiveRoutes() {
        return ResponseEntity.status(200).body(routeService.getActiveRoutes());
    }
    @GetMapping("{employeeId}")
    public ResponseEntity<List<RouteWithTaskDto>> getRoute(@PathVariable String employeeId) {
        return ResponseEntity.status(200).body(routeService.getRoutesByEmployeeId(employeeId));

    }
}


