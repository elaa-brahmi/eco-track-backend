package com.example.demo.controller;
import com.example.demo.dto.CreateEmployeeDto;
import com.example.demo.models.Employee;
import com.example.demo.service.employee.EmployeeService;
import com.example.demo.service.employee.KeycloakAdminService;
import com.example.demo.repositories.EmployeeRepository;



import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    private final KeycloakAdminService keycloakAdminService;
    private final EmployeeRepository employeeRepository;

    /*@PostMapping()
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        return ResponseEntity.status(201).body(employeeService.create(employee));
    }*/

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
    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable String id) {
        employeeService.delete(id);
    }

}
