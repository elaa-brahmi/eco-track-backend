package com.example.demo.service.task;
import com.example.demo.dto.TaskRequest;
import com.example.demo.dto.UpdateTaskRequest;
import com.example.demo.models.Employee;
import com.example.demo.models.Task;

import java.util.List;

public interface TaskService {
    List<Task> findAll();
    Task findById(String id);
    void delete(String id);
}
