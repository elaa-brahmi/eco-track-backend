package com.example.demo.controller;
import com.example.demo.dto.CreateEmployeeDto;
import com.example.demo.models.Employee;
import com.example.demo.service.employee.EmployeeService;
import com.example.demo.service.employee.KeycloakAdminService;
import com.example.demo.repositories.EmployeeRepository;


import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    private final KeycloakAdminService keycloakAdminService;
    private final EmployeeRepository employeeRepository;
    private final Keycloak keycloak;


    @PostMapping()
    public Employee create(@RequestBody CreateEmployeeDto dto) {
        // Create in Keycloak
        String keycloakId = keycloakAdminService.createEmployeeUser(
                dto.getEmail(), dto.getName(), dto.getPassword()
        );

        // Save  record in MongoDB
        Employee emp = Employee.builder()
                .keycloakId(keycloakId)
                .name(dto.getName())
                .email(dto.getEmail())
                .available(true)
                .createdAt(Instant.now())
                .role(dto.getRole())
                .build();

        return employeeRepository.save(emp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id){
        return ResponseEntity.status(200).body(employeeService.findById(id));

    }

    @GetMapping()
    public ResponseEntity<List<Employee>> getAllEmployees(){
        return ResponseEntity.status(200).body(employeeService.findAll());
    }
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable String id, @RequestBody Employee employee) {
        return ResponseEntity.status(200).body(employeeService.update(id, employee));
    }
    @DeleteMapping("/{keycloakId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String keycloakId) {
        Response response = keycloak.realm("springboot-test")
                .users()
                .delete(keycloakId);

        if (response.getStatus() == 404) {
            throw new RuntimeException("Employee not found in Keycloak");
        }
        if (response.getStatus() != 204) {
            throw new RuntimeException("Failed to delete from Keycloak: " + response.getStatus());
        }

        Employee employee = employeeRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("Employee not found in database"));

        employeeRepository.delete(employee);

        return ResponseEntity.noContent().build();
    }

}
