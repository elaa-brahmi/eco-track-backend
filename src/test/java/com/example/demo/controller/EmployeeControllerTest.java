package com.example.demo.controller;

import com.example.demo.config.SecurityConfig;
import com.example.demo.dto.CreateEmployeeDto;
import com.example.demo.models.Employee;
import com.example.demo.models.Role;
import com.example.demo.repositories.EmployeeRepository;
import com.example.demo.service.employee.EmployeeService;
import com.example.demo.service.employee.KeycloakAdminService;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;  // ← Crucial: Static imports for get/post/etc.
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;  // ← Static imports for status/jsonPath/etc.

@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;
    @MockitoBean
    private KeycloakAdminService keycloakAdminService;
    @MockitoBean
    private EmployeeRepository employeeRepository;
    @MockitoBean
    private Keycloak keycloak;

    @Autowired
    private ObjectMapper objectMapper;  // For JSON conversion

    private final String BASE_URL = "/api/employees";
    private static final String KEYCLOAK_ID = "f8e1a2b3-c4d5-6789-abcd-ef1234567890";




    @Test
    void createEmployee_asAdmin_shouldCreateInKeycloakAndMongoDB() throws Exception {
        // GIVEN - DTO from admin
        CreateEmployeeDto dto = new CreateEmployeeDto();
        dto.setName("Mohamed Ali");
        dto.setEmail("mohamed@wasteflow.tn");
        dto.setPassword("Tunis2025!");
        dto.setRole("collector_role");

        // Keycloak returns a fake sub (Keycloak user ID)
        String fakeKeycloakId = "f8e1a2b3-c4d5-6789-abcd-ef1234567890";
        when(keycloakAdminService.createEmployeeUser(
                eq("mohamed@wasteflow.tn"),
                eq("Mohamed Ali"),
                eq("Tunis2025!")
        )).thenReturn(fakeKeycloakId);

        // MongoDB save returns the same object with generated ID
        Employee savedEmployee = Employee.builder()
                .id("emp-123")
                .keycloakId(fakeKeycloakId)
                .name("Mohamed Ali")
                .email("mohamed@wasteflow.tn")
                .available(true)
                .role(Role.collector_role)
                .build();

        when(employeeRepository.save(any(Employee.class)))
                .thenReturn(savedEmployee);

        // WHEN & THEN
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("emp-123"))
                .andExpect(jsonPath("$.keycloakId").value(fakeKeycloakId))
                .andExpect(jsonPath("$.name").value("Mohamed Ali"))
                .andExpect(jsonPath("$.email").value("mohamed@wasteflow.tn"))
                .andExpect(jsonPath("$.role").value("collector_role"))
                .andExpect(jsonPath("$.available").value(true));

        // verify: Keycloak was called
        verify(keycloakAdminService).createEmployeeUser(
                "mohamed@wasteflow.tn", "Mohamed Ali", "Tunis2025!");

        // verify: MongoDB was saved with correct keycloakId
        verify(employeeRepository).save(argThat(emp ->
                fakeKeycloakId.equals(emp.getKeycloakId()) &&
                        "Mohamed Ali".equals(emp.getName()) &&
                        "mohamed@wasteflow.tn".equals(emp.getEmail())
                && "collector_role".equals(emp.getRole().toString())
        ));
    }
    @Test
    void citizenCannotCreateEmployee() throws Exception {
        Employee e1 = Employee.builder().name("ahmed").available(true).build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(e1))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_citizen-role"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEmployeeById() throws Exception {
        Employee c1 = Employee.builder().id("1").name("ahmed").build();
        when(employeeService.findById("1")).thenReturn(c1);
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/{id}", 1)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("ahmed"));

    }

    @Test
    void getAllEmployees() throws Exception {
        Employee e1 = Employee.builder().id("1").name("mourad").available(false).build();
        Employee e2 = Employee.builder().id("2").name("ahmed").available(false).build();

        when(employeeService.findAll()).thenReturn(List.of(e1, e2));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))

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
                .role(Role.collector_role)
                .available(false)
                .build();

        Employee updated = Employee.builder()
                .id("7")
                .name("new name")
                .role(Role.collector_role)
                .available(true)
                .build();

        when(employeeService.update(eq("7"), any(Employee.class))).thenReturn(updated);

        mockMvc.perform(put(BASE_URL + "/{id}", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("7"))
                .andExpect(jsonPath("$.name").value("new name"))
                .andExpect(jsonPath("$.role").value("collector_role"))
                .andExpect(jsonPath("$.available").value(true));

        verify(employeeService).update(eq("7"), any(Employee.class));

    }

    @Test
    void deleteEmployee() throws Exception {
        // GIVEN: Mock the EXACT chain your controller uses
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);

        when(keycloak.realm("springboot-test")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.delete(KEYCLOAK_ID))
                .thenReturn(Response.noContent().build());

        Employee employee = Employee.builder()
                .id("emp-123")
                .keycloakId(KEYCLOAK_ID)
                .name("Sami")
                .email("sami@wasteflow.tn")
                .build();

        when(employeeRepository.findByKeycloakId(KEYCLOAK_ID))
                .thenReturn(Optional.of(employee));

        mockMvc.perform(delete("/api/employees/{keycloakId}", KEYCLOAK_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))
                .andExpect(status().isNoContent());

        verify(usersResource).delete(KEYCLOAK_ID);
        verify(employeeRepository).delete(employee);
    }
    @Test
    void getEmployeeByKeycloakId_returnsEmployee() throws Exception {
        Employee emp = new Employee();
        emp.setId("1");
        emp.setKeycloakId("kc123");
        emp.setName("Elaa Brahmi");

        when(employeeService.findByKeycloakId("kc123")).thenReturn(emp);

        mockMvc.perform(get("/api/employees/employee/kc123")
                        .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_employee-role"))))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keycloakId").value("kc123"))
                .andExpect(jsonPath("$.name").value("Elaa Brahmi"));
    }
}