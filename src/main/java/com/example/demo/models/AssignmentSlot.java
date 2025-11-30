package com.example.demo.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
@Document(collection = "AssignmentSlots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSlot {
    private String taskId;
    private Instant start;
    private Instant end;
}

