package com.example.demo.controller;

import com.example.demo.dto.UpdateTaskRequest;
import com.example.demo.models.Employee;
import com.example.demo.models.Task;
import com.example.demo.service.task.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/tasks";

    @Test
    void getAllTasks() throws Exception {
        Task t1 = Task.builder().id("1").title("Collect plastic - La Marsa").status("NEW").build();
        Task t2 = Task.builder().id("2").title("Empty glass bin - Sfax").status("ASSIGNED").build();

        when(taskService.findAll()).thenReturn(List.of(t1, t2));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Collect plastic - La Marsa"))
                .andExpect(jsonPath("$[1].status").value("ASSIGNED"));

        verify(taskService).findAll();
    }

    @Test
    void getTaskById() throws Exception {
        Task task = Task.builder()
                .id("99")
                .title("Urgent: Overflow in Carthage")
                .status("NEW")
                .createdAt(Instant.now())
                .build();

        when(taskService.findById("99")).thenReturn(task);

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("99"))
                .andExpect(jsonPath("$.title").value("Urgent: Overflow in Carthage"))
                .andExpect(jsonPath("$.status").value("NEW"));

        verify(taskService).findById("99");
    }

    @Test
    void createTask() throws Exception {
        Task input = Task.builder()
                .title("Clean up illegal dump")
                .containerId("cont-123")
                .build();

        Task saved = Task.builder()
                .id("task-001")
                .title("Clean up illegal dump")
                .containerId("cont-123")
                .status("NEW")
                .createdAt(Instant.now())
                .build();

        when(taskService.create(any(Task.class))).thenReturn(saved);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())  // 201
                .andExpect(jsonPath("$.id").value("task-001"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.title").value("Clean up illegal dump"));

        verify(taskService).create(any(Task.class));
    }

    @Test
    void assignToTask() throws Exception {
        Employee employee = Employee.builder().id("emp-007").name("Karim").build();

        Task assignedTask = Task.builder()
                .id("t-55")
                .title("Collect from Ariana")
                .assignedTo("emp-007")
                .status("ASSIGNED")
                .build();

        when(taskService.assign(eq("t-55"), any(Employee.class))).thenReturn(assignedTask);

        mockMvc.perform(patch(BASE_URL + "/t-55/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedTo").value("emp-007"))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        verify(taskService).assign(eq("t-55"), any(Employee.class));
    }
    @Test
    void updateTask() throws Exception {
        Task updated = Task.builder()
                .id("t-88")
                .title("Old title")
                .status("IN_PROGRESS")
                .assignedTo("emp-007")
                .build();

        when(taskService.updateStatus(eq("t-88"), any(UpdateTaskRequest.class))).thenReturn(updated);

        String json = """
            {
                "status": "IN_PROGRESS",
                "assignedTo": "emp-007"
            }
            """;

        mockMvc.perform(put(BASE_URL + "/t-88")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.assignedTo").value("emp-007"));

        verify(taskService).updateStatus(eq("t-88"), argThat(req ->
                "IN_PROGRESS".equals(req.status())
        ));
    }

    @Test
    void deleteTask() throws Exception {
        doNothing().when(taskService).delete("del-123");

        mockMvc.perform(delete(BASE_URL + "/del-123"))
                .andExpect(status().isNoContent());

        verify(taskService).delete("del-123");
    }
}