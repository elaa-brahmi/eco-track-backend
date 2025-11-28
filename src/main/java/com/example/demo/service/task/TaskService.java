package com.example.demo.service.task;
import com.example.demo.dto.TaskRequest;
import com.example.demo.dto.UpdateTaskRequest;
import com.example.demo.models.Employee;
import com.example.demo.models.Task;

import java.util.List;
import java.util.Optional;

public interface TaskService {
    List<Task> findAll();
    Task findById(String id);
    void delete(String id);
    List<Task> getTasksByEmployeeId(String employeeId);
}
