package com.example.demo.controller;
import com.example.demo.models.Vehicle;
import com.example.demo.service.vehicule.VehicleService;
import com.example.demo.service.vehicule.VehicleServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
public class VehiculeController {
    private final VehicleService vehicleService;
    @PostMapping()
    public ResponseEntity<Vehicle> addVehicle(@RequestBody Vehicle vehicle) {
        return ResponseEntity.status(201).body(vehicleService.create(vehicle));

    }
    @GetMapping()
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.status(200).body(vehicleService.findAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable String id) {
        return ResponseEntity.status(200).body(vehicleService.findById(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable String id, @RequestBody Vehicle vehicle) {
        return ResponseEntity.status(200).body(vehicleService.update(id, vehicle));
    }
    @DeleteMapping("/{id}")
    public void deleteVehicle(@PathVariable String id) {
        vehicleService.delete(id);
    }
}
