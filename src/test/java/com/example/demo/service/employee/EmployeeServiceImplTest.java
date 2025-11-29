package com.example.demo.service.employee;
import com.example.demo.models.Employee;
import com.example.demo.repositories.EmployeeRepository;
import com.example.demo.service.employee.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private EmployeeServiceImpl service;


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
    @Test
    void findByKeycloakId_returnsEmployee() {
        Employee emp = new Employee();
        emp.setId("123");
        emp.setKeycloakId("kc123");

        when(repository.findByKeycloakId("kc123"))
                .thenReturn(Optional.of(emp));

        Employee result = service.findByKeycloakId("kc123");

        assertNotNull(result);
        assertEquals("kc123", result.getKeycloakId());
    }
    @Test
    void findByKeycloakId_throwsExceptionWhenNotFound() {
        when(repository.findByKeycloakId("missing"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.findByKeycloakId("missing"));

        assertEquals("Employee not registered in system", ex.getMessage());
    }

}
