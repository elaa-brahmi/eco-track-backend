package com.example.demo.dto;

import com.example.demo.models.Container;
import com.example.demo.models.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


public class OptimizeRequest {
    private VehicleRequest vehicle;
    private List<ContainerRequest> containers;

    public OptimizeRequest() {}  // REQUIRED

    public VehicleRequest getVehicle() { return vehicle; }
    public void setVehicle(VehicleRequest vehicle) { this.vehicle = vehicle; }
    public List<ContainerRequest> getContainers() { return containers; }
    public void setContainers(List<ContainerRequest> containers) { this.containers = containers; }
}