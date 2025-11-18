package com.example.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskRequest(
    @NotNull(message="100")
    @NotEmpty(message="100")
    String status
){}


