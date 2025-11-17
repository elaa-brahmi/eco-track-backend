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
    private String location;

    private String status; // NEW, IN_PROGRESS, RESOLVED

    private Instant createdAt;
}