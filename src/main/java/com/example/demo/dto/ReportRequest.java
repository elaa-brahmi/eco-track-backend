package com.example.demo.dto;

import lombok.Data;

@Data
public class ReportRequest {
    private String description;
    private double[] location;
}
