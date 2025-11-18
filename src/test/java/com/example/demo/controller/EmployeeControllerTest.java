package com.example.demo.controller;

import com.example.demo.models.Container;
import com.example.demo.models.Employee;
import com.example.demo.service.container.ContainerService;
import com.example.demo.service.employee.EmployeeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;  // ← Crucial: Static imports for get/post/etc.
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;  // ← Static imports for status/jsonPath/etc.

@WebMvcTest(EmployeeController.class)  // Only loads controller layer
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;  // For JSON conversion

    private final String BASE_URL = "/api/employees";


    @Test
    void createEmployee() throws Exception {
        //input without id
        Employee e1=Employee.builder().name("ahmed").available(true).build();
        //what the service returns + generated id
        Employee savedEmp = Employee.builder().id("1").name("ahmed").available(true).build();
        when(employeeService.create(any(Employee.class))).thenReturn(savedEmp);
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(e1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("ahmed"))
                .andExpect(jsonPath("$.available").value(true));
        verify(employeeService,times(1)).create(any(Employee.class));

    }

    @Test
    void getEmployeeById() throws Exception {
        Employee c1 = Employee.builder().id("1").name("ahmed").build();
        when(employeeService.findById("1")).thenReturn(c1);
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("ahmed"));

    }

    @Test
    void getAllEmployees() throws Exception {
        Employee e1 = Employee.builder().id("1").name("mourad").available(false).build();
        Employee e2 = Employee.builder().id("2").name("ahmed").available(false).build();

        when(employeeService.findAll()).thenReturn(List.of(e1, e2));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].name").value("ahmed"));

        verify(employeeService).findAll();
    }

    @Test
    void updateEmployee() throws Exception {
        Employee existing = Employee.builder()
                .id("7")
                .name("old name")
                .role("driver")
                .available(false)
                .build();

        Employee updated = Employee.builder()
                .id("7")
                .name("new name")
                .role("supervisor")
                .available(true)
                .build();

        when(employeeService.update(eq("7"), any(Employee.class))).thenReturn(updated);

        mockMvc.perform(put(BASE_URL + "/{id}", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("7"))
                .andExpect(jsonPath("$.name").value("new name"))
                .andExpect(jsonPath("$.role").value("supervisor"))
                .andExpect(jsonPath("$.available").value(true));

        verify(employeeService).update(eq("7"), any(Employee.class));

    }

    @Test
    void deleteEmployee()
            throws Exception {
        doNothing().when(employeeService).delete("42");

        mockMvc.perform(delete(BASE_URL + "/{id}", "42"))
                .andExpect(status().isOk());
        verify(employeeService, times(1)).delete("42");

    }
}