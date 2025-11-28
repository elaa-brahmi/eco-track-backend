package com.example.demo.controller;
import com.example.demo.dto.ResolveReportRequest;
import com.example.demo.dto.ResolveReportResponse;
import com.example.demo.dto.TaskRequest;
import com.example.demo.dto.UpdateTaskRequest;
import com.example.demo.models.Employee;
import com.example.demo.models.Task;
import com.example.demo.service.task.TaskAssignmentService;
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
    private final TaskAssignmentService taskAssignmentService;

    @GetMapping()
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.status(200).body(taskService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable String id) {
        return ResponseEntity.status(200).body(taskService.findById(id));
    }

   @PostMapping("/{reportId}/resolve")
   public ResponseEntity<ResolveReportResponse> resolveReport(
           @PathVariable String reportId,
           @RequestBody ResolveReportRequest req
   ) {
       ResolveReportResponse response = taskAssignmentService.resolveReport(reportId, req);
       return ResponseEntity.ok(response);
   }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<String> completeTask(@PathVariable String taskId) {
        taskAssignmentService.completeTask(taskId);
        return ResponseEntity.ok("Task completed. Employees and vehicle are now free.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
