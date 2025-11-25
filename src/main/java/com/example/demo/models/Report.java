package com.example.demo.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("reports")
public class Report {

    @Id
    private String id;

    private String description;
    private String photoUrl;
    private double[] location;

    private ReportStatus status; // NEW, IN_PROGRESS, RESOLVED
    private ReportType type;

    private Instant createdAt;
}