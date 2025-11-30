package com.example.demo.controller;

import com.example.demo.dto.OptimizeRequest;
import com.example.demo.dto.RouteSolution;
import com.example.demo.models.Container;
import com.example.demo.models.Vehicle;
import com.example.demo.service.routing.RouteOptimizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class RouteController {

    private final RouteOptimizationService optimizer;
    @GetMapping()
    public ResponseEntity<String> getRoutes() {
        return ResponseEntity.ok("Route Optimization Service");
    }

    @PostMapping("/optimize")
    public ResponseEntity<RouteSolution> optimize(@RequestBody OptimizeRequest request) {
        System.out.println("=== DEBUG: Request received ===");
        System.out.println("Vehicle ID: " + (request.getVehicle() != null ? request.getVehicle().getId() : "NULL"));
        System.out.println("Containers count: " + (request.getContainers() != null ? request.getContainers().size() : "NULL"));
        System.out.println("Full request: " + request);  // This will now print properly

        // Quick null check
        if (request.getVehicle() == null || request.getContainers() == null) {
            System.out.println("=== ERROR: Vehicle or Containers is null ===");
            return ResponseEntity.badRequest().build();
        }

        // Convert DTO → entities
        Vehicle truck = new Vehicle();
        truck.setId(request.getVehicle().getId());
        truck.setName(request.getVehicle().getName());
        truck.setCapacity(request.getVehicle().getCapacity());
        truck.setLocation(toDoubleArray(request.getVehicle().getLocation()));

        List<Container> containers = request.getContainers().stream()
                .map(dto -> {
                    Container c = new Container();
                    c.setId(dto.getId());
                    c.setLocation(toDoubleArray(dto.getLocation()));
                    return c;
                })
                .collect(Collectors.toList());

        System.out.println("=== DEBUG: Converted truck capacity: " + truck.getCapacity());
        System.out.println("=== DEBUG: Calling optimizer ===");

        RouteSolution solution = optimizer.optimizeRoute(containers, truck);
        return ResponseEntity.ok(solution);
    }

    private double[] toDoubleArray(List<Double> list) {
        if (list == null || list.size() < 2) {
            System.out.println("=== WARNING: Invalid location array ===");
            return new double[]{0.0, 0.0};
        }
        double[] arr = new double[]{list.get(0), list.get(1)};
        System.out.println("=== DEBUG: Converted location: [" + arr[0] + ", " + arr[1] + "]");
        return arr;
    }
}


