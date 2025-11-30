package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TaskRequest {
    private List<String> containersIDs;
    private String vehiculeId;

    private List<String> employeesIDs;
    private String title;

    private String status;
    private String priority;
    private Instant dueDate;
    private String reportId;
}
