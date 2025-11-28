package com.example.demo.service.task;

import com.example.demo.dto.TaskRequest;
import com.example.demo.dto.UpdateTaskRequest;
import com.example.demo.models.*;
import com.example.demo.repositories.ContainerRepository;
import com.example.demo.repositories.EmployeeRepository;
import com.example.demo.repositories.TaskRepository;
import com.example.demo.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {


    private final TaskRepository repository;

    @Override
    public List<Task> findAll() {
        return repository.findAll();
    }

    @Override
    public Task findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }
}