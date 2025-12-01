package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
// ContainerRequest.java
public class ContainerRequest {
    private String id;
    private List<Double> location;

    public ContainerRequest() {}  // REQUIRED

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public List<Double> getLocation() { return location; }
    public void setLocation(List<Double> location) { this.location = location; }
}