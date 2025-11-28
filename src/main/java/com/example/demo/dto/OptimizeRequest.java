package com.example.demo.dto;

import com.example.demo.models.Container;
import com.example.demo.models.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptimizeRequest {
    private Vehicle vehicle;
    private List<Container> containers;
}
