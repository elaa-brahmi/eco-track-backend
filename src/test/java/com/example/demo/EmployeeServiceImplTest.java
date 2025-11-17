package com.example.demo;
import com.example.demo.models.Employee;
import com.example.demo.repositories.EmployeeRepository;
import com.example.demo.service.employee.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private EmployeeServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreate() {
        Employee e = new Employee();
        when(repository.save(any())).thenReturn(e);

        Employee result = service.create(e);

        assertTrue(result.isAvailable());
    }

    @Test
    void testUpdate() {
        Employee existing = new Employee();
        existing.setId("e1");

        Employee updated = new Employee();
        updated.setName("John");

        when(repository.findById("e1")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        Employee result = service.update("e1", updated);

        assertEquals("John", result.getName());
    }
}
