package com.example.demo.service.employee;

import com.example.demo.models.Employee;
import com.example.demo.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Override
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee findById(String id) {
        return employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("employee not found"));
    }
    @Override
    public Employee findByKeycloakId(String keycloakId) {
        return employeeRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("Employee not registered in system"));
    }

    @Override
    public Employee create(Employee e) {
        e.setAvailable(true);//default
        return employeeRepository.save(e);
    }

    @Override
    public Employee update(String id, Employee e) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("employee not found"));
        employee.setName(e.getName());
        employee.setAvailable(e.isAvailable());
        employee.setRole(e.getRole());
        return employeeRepository.save(employee);
    }

    @Override
    public void delete(String id) {
        employeeRepository.deleteById(id);

    }
}
