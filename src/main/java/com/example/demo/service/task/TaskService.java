package com.example.demo.service.task;
import com.example.demo.models.Employee;
import com.example.demo.models.Task;

import java.util.List;

public interface TaskService {
    List<Task> findAll();
    Task findById(String id);
    Task create(Task task);
    Task assign(String taskId, Employee employee);
    Task updateStatus(String taskId, String status);
    void delete(String id);
}
