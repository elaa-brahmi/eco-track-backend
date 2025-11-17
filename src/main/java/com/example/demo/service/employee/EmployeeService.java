package com.example.demo.service.employee;

import com.example.demo.models.Container;
import com.example.demo.models.Employee;

import java.util.List;

public interface EmployeeService {
    List<Employee> findAll();
    Employee findById(String id);
    Employee create(Employee e);
    Employee update(String id, Employee e);
    void delete(String id);
}
