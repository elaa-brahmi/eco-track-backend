package com.example.demo.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

//Task Model (AI-generated tasks + manual tasks)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("tasks")
public class Task {

    @Id
    private String id;
    private List<String> containersIDs;
    private String vehiculeId;

    private List<String> employeesIDs;
    private String title;

    private TaskStatus status;     // pending, IN_PROGRESS, DONE
    private TaskPriority priority;   // LOW, MEDIUM, HIGH
    private Instant dueDate;
    private String reportId;

    private Instant createdAt;
}
