package com.example.demo.utils;

import com.example.demo.models.*;
import com.example.demo.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SensorSimulatorTest {

    @Mock
    private ContainerRepository containerRepo;

    @Mock
    private EmployeeRepository employeeRepo;

    @Mock
    private VehicleRepository vehicleRepo;

    @Mock
    private TaskRepository taskRepo;

    @Mock
    private SimpMessagingTemplate ws;

    private SensorSimulator simulator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        simulator = new SensorSimulator(containerRepo, ws, containerRepo, employeeRepo, vehicleRepo, taskRepo, ws);
    }

    @Test
    void simulate_createsTaskWhenEnoughHighFillContainers() {
        // Arrange: create 4 containers above 75% fill
        Container c1 = new Container(); c1.setId("c1"); c1.setFillLevel(80); c1.setStatus("half fill");
        Container c2 = new Container(); c2.setId("c2"); c2.setFillLevel(85); c2.setStatus("half fill");
        Container c3 = new Container(); c3.setId("c3"); c3.setFillLevel(90); c3.setStatus("half fill");
        Container c4 = new Container(); c4.setId("c4"); c4.setFillLevel(95); c4.setStatus("half fill");

        Employee loader = Employee.builder().id("loader1").available(true).role(Role.loader_role).build();
        Employee driver = Employee.builder().id("driver1").available(true).role(Role.driver_role).build();

        when(containerRepo.findAll()).thenReturn(List.of(c1, c2, c3, c4));
        when(employeeRepo.findByRoleAndAvailableTrue(Role.loader_role)).thenReturn(List.of(loader));
        when(employeeRepo.findByRoleAndAvailableTrue(Role.driver_role)).thenReturn(List.of(driver));

        // Mock findById for blockAvailability
        when(employeeRepo.findById("loader1")).thenReturn(Optional.of(loader));
        when(employeeRepo.findById("driver1")).thenReturn(Optional.of(driver));

        Vehicle vehicle = Vehicle.builder().id("v1").available(true).capacity(5).build();
        when(vehicleRepo.findByAvailableTrue()).thenReturn(List.of(vehicle));
        when(vehicleRepo.findById("v1")).thenReturn(Optional.of(vehicle));

        // Act
        simulator.simulate();

        // Assert
        verify(containerRepo, times(4)).save(any(Container.class));

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepo).save(taskCaptor.capture());
        Task createdTask = taskCaptor.getValue();

        assertEquals("Auto collection", createdTask.getTitle());
        assertEquals(TaskStatus.PENDING, createdTask.getStatus());
        assertEquals(2, createdTask.getEmployeesIDs().size()); // 1 loader + 1 driver
        assertEquals("v1", createdTask.getVehiculeId());
        assertEquals(4, createdTask.getContainersIDs().size()); // all 4 containers assigned

        // Verify employees and vehicle blocked
        verify(employeeRepo, times(2)).save(any(Employee.class));
        verify(vehicleRepo).save(vehicle);

        // Verify WebSocket sent
        verify(ws).convertAndSend(eq("/topic/tasks"), any(Task.class));
        verify(ws, times(4)).convertAndSend(eq("/topic/containers"), any(Container.class));
    }

    @Test
    void simulate_doesNotCreateTaskWhenNotEnoughHighFillContainers() {
        // Arrange: only 3 containers above 75%
        Container c1 = new Container(); c1.setId("c1"); c1.setFillLevel(80);
        Container c2 = new Container(); c2.setId("c2"); c2.setFillLevel(85);
        Container c3 = new Container(); c3.setId("c3"); c3.setFillLevel(70); // below threshold
        when(containerRepo.findAll()).thenReturn(List.of(c1, c2, c3));

        // Act
        simulator.simulate();

        // Assert
        verify(taskRepo, never()).save(any());
        verify(ws, times(3)).convertAndSend(eq("/topic/containers"), any(Container.class));
    }

}
