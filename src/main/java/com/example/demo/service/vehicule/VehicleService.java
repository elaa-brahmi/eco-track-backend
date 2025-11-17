package com.example.demo.service.vehicule;

import com.example.demo.models.Vehicle;

import java.util.List;

public interface VehicleService {
    List<Vehicle> findAll();
    Vehicle findById(String id);
    Vehicle create(Vehicle vehicle);
    Vehicle update(String id, Vehicle updated);
    void delete(String id);
}
