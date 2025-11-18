package com.example.demo.service.task;
import com.example.demo.models.Employee;
import com.example.demo.models.Task;
import com.example.demo.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskServiceImpl service;


    @Test
    void testCreate() {
        Task t = new Task();
        when(repository.save(any())).thenReturn(t);

        Task result = service.create(t);

        assertEquals("NEW", result.getStatus());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void testAssign() {
        Task t = new Task();
        t.setId("t1");

        when(repository.findById("t1")).thenReturn(Optional.of(t));
        when(repository.save(any())).thenReturn(t);
        Employee e = new Employee();
        Employee e2 = new Employee();
        e2.setId("e2");

        Task updated = service.assign("t1", e2);

        assertEquals(e2.getId(), updated.getAssignedTo());
        assertEquals("ASSIGNED", updated.getStatus());
    }
}
