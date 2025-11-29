package com.example.demo.models;



import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "TaskRequirements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequirement {
    private int collectors;
    private int loaders;
    private int drivers;
    private int maintenance;
}
