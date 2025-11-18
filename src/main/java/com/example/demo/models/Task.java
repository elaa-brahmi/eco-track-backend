package com.example.demo.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
//Task Model (AI-generated tasks + manual tasks)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("tasks")
public class Task {

    @Id
    private String id;

    private String type;       // COLLECTION or INCIDENT
    private String containerId;

    private String assignedTo; // employeeId
    private String title;

    private String status;     // NEW, ASSIGNED, IN_PROGRESS, DONE
    private String priority;   // LOW, MEDIUM, HIGH

    private Instant createdAt;
}
