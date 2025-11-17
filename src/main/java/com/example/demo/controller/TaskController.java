package com.example.demo.controller;
import com.example.demo.models.Employee;
import com.example.demo.models.Task;
import com.example.demo.service.task.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping()
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.status(200).body(taskService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable String id) {
        return ResponseEntity.status(200).body(taskService.findById(id));
    }

    @PostMapping()
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        return ResponseEntity.status(201).body(taskService.create(task));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<Task> assignToTask(@PathVariable String id, @RequestBody Employee employee) {
        return ResponseEntity.status(200).body(taskService.assign(id, employee));

    }
    @PutMapping("/{id}/status")
    public ResponseEntity<Task> updateTask(@PathVariable String id, @RequestBody String status) {
        return ResponseEntity.status(200).body(taskService.updateStatus(id, status));

    }
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable String id) {
        taskService.delete(id);
    }
}
