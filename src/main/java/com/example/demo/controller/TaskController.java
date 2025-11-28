package com.example.demo.controller;
import com.example.demo.dto.TaskRequest;
import com.example.demo.dto.UpdateTaskRequest;
import com.example.demo.models.Employee;
import com.example.demo.models.Task;
import com.example.demo.service.task.TaskService;
import jakarta.validation.Valid;
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


    @PatchMapping("/{id}/assign")
    public ResponseEntity<Task> assignToTask( @RequestBody TaskRequest task) {
        return ResponseEntity.status(200).body(taskService.assign(task));

    }
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable String id, @RequestBody @Valid UpdateTaskRequest request) {
        return ResponseEntity.status(200).body(taskService.updateStatus(id, request));

    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
