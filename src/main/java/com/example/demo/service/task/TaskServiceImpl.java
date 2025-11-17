package com.example.demo.service.task;

import com.example.demo.models.Employee;
import com.example.demo.models.Task;
import com.example.demo.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
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
    public Task create(Task task) {
        task.setCreatedAt(Instant.now());
        task.setStatus("NEW");
        return repository.save(task);
    }

    @Override
    public Task assign(String taskId, Employee employee) {
        Task task = findById(taskId);
        task.setAssignedTo(employee.getId());
        task.setStatus("ASSIGNED");
        return repository.save(task);
    }

    @Override
    public Task updateStatus(String taskId, String status) {
        Task task = findById(taskId);
        task.setStatus(status);
        return repository.save(task);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }
}