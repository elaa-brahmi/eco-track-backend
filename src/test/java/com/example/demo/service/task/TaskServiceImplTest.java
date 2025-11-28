package com.example.demo.service.task;
import com.example.demo.dto.ResolveReportRequest;
import com.example.demo.dto.ResolveReportResponse;
import com.example.demo.models.*;
import com.example.demo.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock private ReportRepository reportRepo;
    @Mock private ContainerRepository containerRepo;
    @Mock private EmployeeRepository employeeRepo;
    @Mock private VehicleRepository vehicleRepo;
    @Mock private TaskRepository taskRepo;
    @Mock private SimpMessagingTemplate ws;

    @InjectMocks
    private TaskAssignmentService service;
    @InjectMocks
    private TaskServiceImpl taskService;

    Report report;
    Container container;
    Employee collector;
    Vehicle vehicle;
    Task savedTask;

    @BeforeEach
    void init(){
        report = new Report();
        report.setId("r1");
        report.setLocation(new double[]{10.0, 10.0});
        report.setStatus(ReportStatus.NEW);

        container = new Container();
        container.setId("c1");
        container.setLocation(new double[]{10.1, 10.1});

        collector = new Employee();
        collector.setId("e1");
        collector.setRole(Role.collector_role);
        collector.setAvailable(true);
        collector.setSchedule(new ArrayList<>());

        vehicle = new Vehicle();
        vehicle.setId("v1");
        vehicle.setAvailable(true);
        vehicle.setLocation(new double[]{10.2, 10.2});
        vehicle.setSchedule(new ArrayList<>());

        savedTask = new Task();
        savedTask.setId("t1");

    }

    @Test
    void createTask() {
        //Given
        ResolveReportRequest request = new ResolveReportRequest();
        request.setTaskTitle("Clean bin");
        request.setPriority(TaskPriority.HIGH);

        TaskRequirement tr = new TaskRequirement();
        tr.setCollectors(1);
        tr.setLoaders(0);
        tr.setDrivers(0);
        tr.setMaintenance(0);
        request.setRequirement(tr);
        request.setStart(Instant.now());
        request.setEnd(Instant.now().plusSeconds(3600));

        // ---------- MOCK DATABASE CALLS ----------
        when(reportRepo.findById("r1")).thenReturn(Optional.of(report));
        when(containerRepo.findAll()).thenReturn(List.of(container));
        when(employeeRepo.findByRoleAndAvailableTrue(Role.collector_role))
                .thenReturn(List.of(collector));
        when(employeeRepo.findById("e1")).thenReturn(Optional.of(collector));
        when(vehicleRepo.findByAvailableTrue()).thenReturn(List.of(vehicle));
        when(vehicleRepo.findById("v1")).thenReturn(Optional.of(vehicle));
        when(taskRepo.save(any(Task.class))).thenReturn(savedTask);

        //when
        ResolveReportResponse response = service.resolveReport("r1", request);
        //then
        assertEquals("t1", response.getTaskId());
        assertEquals("task assigned", response.getMessage());

        verify(ws).convertAndSend(eq("/topic/tasks"), any(Task.class));
        verify(employeeRepo).save(any(Employee.class));
        verify(vehicleRepo).save(any(Vehicle.class));

        verify(reportRepo).save(report);
        assertEquals(ReportStatus.Under_Review, report.getStatus());

    }
    @Test
    void resolveReport_noContainerFound() {
        when(reportRepo.findById("r1")).thenReturn(Optional.of(report));
        when(containerRepo.findAll()).thenReturn(List.of());

        ResolveReportRequest req = new ResolveReportRequest();

        assertThrows(RuntimeException.class, () ->
                service.resolveReport("r1", req)
        );
    }
    @Test
    void completeTask() {
        // Arrange task
        Task task = new Task();
        task.setId("t1");
        task.setEmployeesIDs(List.of("e1"));
        task.setVehiculeId("v1");
        task.setReportId("r1");

        AssignmentSlot slot = new AssignmentSlot("t1", Instant.now(), Instant.now().plusSeconds(100));

        // Employee setup
        collector.setAvailable(false);
        collector.setSchedule(new ArrayList<>(List.of(slot)));

        // Vehicle setup
        vehicle.setAvailable(false);
        vehicle.setSchedule(new ArrayList<>(List.of(slot)));

        // Report setup
        Report report = new Report();
        report.setId("r1");
        report.setStatus(ReportStatus.Under_Review);

        // Mock repository calls
        when(taskRepo.findById("t1")).thenReturn(Optional.of(task));
        when(employeeRepo.findById("e1")).thenReturn(Optional.of(collector));
        when(vehicleRepo.findById("v1")).thenReturn(Optional.of(vehicle));
        when(reportRepo.findById("r1")).thenReturn(Optional.of(report));

        // Act
        service.completeTask("t1");

        // Assert employee
        assertTrue(collector.isAvailable());
        assertTrue(collector.getSchedule().isEmpty());

        // Assert vehicle
        assertTrue(vehicle.isAvailable());
        assertTrue(vehicle.getSchedule().isEmpty());

        // Assert task
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        verify(taskRepo).save(task);

        // Assert report
        assertEquals(ReportStatus.RESOLVED, report.getStatus());
        verify(reportRepo).save(report);
    }

    @Test
    void testFindAll() {
        List<Task> tasks = Arrays.asList(new Task(), new Task());
        when(taskRepo.findAll()).thenReturn(tasks);

        List<Task> result = taskRepo.findAll();

        assertEquals(2, result.size());
        verify(taskRepo, times(1)).findAll();
    }

    @Test
    void testFindById() {
        Task task = new Task();
        task.setId("t1");

        when(taskRepo.findById("t1")).thenReturn(Optional.of(task));

        Task result = taskService.findById("t1");

        assertNotNull(result);
        assertEquals("t1", result.getId());
        verify(taskRepo, times(1)).findById("t1");
    }

    @Test
    void testFindById_NotFound() {
        when(taskRepo.findById("t1")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> taskService.findById("t1"));
        assertEquals("Task not found", ex.getMessage());

        verify(taskRepo, times(1)).findById("t1");
    }

    @Test
    void testDelete() {
        taskService.delete("t1");
        verify(taskRepo, times(1)).deleteById("t1");
    }

    @Test
    void getTasksByEmployeeId_returnsTasksForEmployee() {
        Task task1 = new Task();
        task1.setId("t1");
        task1.setEmployeesIDs(List.of("e1", "e2"));

        Task task2 = new Task();
        task2.setId("t2");
        task2.setEmployeesIDs(List.of("e3"));

        Task task3 = new Task();
        task3.setId("t3");
        task3.setEmployeesIDs(null);

        when(taskRepo.findAll()).thenReturn(List.of(task1, task2, task3));

        List<Task> result = taskService.getTasksByEmployeeId("e1");

        assertEquals(1, result.size());
        assertEquals("t1", result.get(0).getId());
    }

    @Test
    void getTasksByEmployeeId_returnsEmptyListIfNoTasks() {
        when(taskRepo.findAll()).thenReturn(List.of());

        List<Task> result = taskService.getTasksByEmployeeId("e1");

        assertEquals(0, result.size());
    }




}
