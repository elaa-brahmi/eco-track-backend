package com.example.demo.controller;

import com.example.demo.dto.ResolveReportResponse;

import com.example.demo.models.Task;
import com.example.demo.service.task.TaskAssignmentService;
import com.example.demo.service.task.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;

import static org.mockito.ArgumentMatchers.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskAssignmentService service;
    @MockitoBean
    private TaskService Taskservice;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void resolveReport() throws Exception {

        ResolveReportResponse mockResp =
                new ResolveReportResponse("t1", "task assigned");

        when(service.resolveReport(eq("r1"), any()))
                .thenReturn(mockResp);

        mockMvc.perform(post("/api/tasks/r1/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskTitle": "Clean",
                                  "priority": "HIGH",
                                  "start": "2025-01-01T00:00:00Z",
                                  "end": "2025-01-01T01:00:00Z",
                                  "requirement": {
                                    "collectors": 1,
                                    "loaders": 0,
                                    "drivers": 0,
                                    "maintenance": 0
                                  }
                                }
                                """)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_admin-role")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value("t1"))
                .andExpect(jsonPath("$.message").value("task assigned"));
    }
    @Test
    void completeTask() throws Exception {
        String taskId = "task123";

        mockMvc.perform(post("/api/tasks/tasks/{taskId}/complete", taskId)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_employee-role")
                        )))
                .andExpect(status().isOk())
                .andExpect(content().string("Task completed. Employees and vehicle are now free."));

        verify(service, times(1)).completeTask(taskId);
    }
    @Test
    void getTasksByEmployeeId_returnsTasks() throws Exception {
        Task task = new Task();
        task.setId("t1");
        task.setEmployeesIDs(List.of("e1"));

        when(Taskservice.getTasksByEmployeeId("e1")).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks/employees/e1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_employee-role")
                        )))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("t1"));
    }

    @Test
    void getTasksByEmployeeId_returnsEmptyList() throws Exception {
        when(Taskservice.getTasksByEmployeeId("e2")).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks/employees/e2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_employee-role")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

}
