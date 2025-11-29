package com.example.demo.dto;

import com.example.demo.models.TaskPriority;
import com.example.demo.models.TaskRequirement;
import lombok.Data;

import java.time.Instant;

@Data
public class ResolveReportRequest {
    private String taskTitle;
    private TaskPriority priority;
    private TaskRequirement requirement;
    private Instant start;
    private Instant end;
}
