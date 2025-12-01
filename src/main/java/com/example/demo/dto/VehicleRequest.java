package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// VehicleRequest.java
public class VehicleRequest {
    private String id;
    private String name;
    private List<Double> location;
    private int capacity;

    public VehicleRequest() {}  // REQUIRED

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Double> getLocation() { return location; }
    public void setLocation(List<Double> location) { this.location = location; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}