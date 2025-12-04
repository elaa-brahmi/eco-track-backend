package com.example.demo.utils;

import com.example.demo.dto.RouteSolution;
import com.example.demo.models.*;
import com.example.demo.repositories.*;
import com.example.demo.service.routing.RouteOptimizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SensorSimulatorTest {

    @Mock
    private ContainerRepository containerRepo;

    @Mock
    private EmployeeRepository employeeRepo;
    @Mock
    private  RouteOptimizationService optimizer;

    @Mock
    private VehicleRepository vehicleRepo;
    @Mock
    private RouteRepository routeRepo;

    @Mock
    private TaskRepository taskRepo;

    @Mock
    private SimpMessagingTemplate ws;

    private SensorSimulator simulator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        simulator = new SensorSimulator(containerRepo,ws,  employeeRepo, vehicleRepo, taskRepo, optimizer,routeRepo);
    }



    @Test
    void simulate_createsTaskAndOptimalRoute_whenEnoughFreeFullContainers() {
        // ARRANGE: 4 full containers → none in PENDING task
        Container c1 = Container.builder().id("c1").fillLevel(80).location(new double[]{36.8, 10.1}).build();
        Container c2 = Container.builder().id("c2").fillLevel(95).location(new double[]{36.81, 10.16}).build();
        Container c3 = Container.builder().id("c3").fillLevel(88).location(new double[]{36.80, 10.15}).build();
        Container c4 = Container.builder().id("c4").fillLevel(92).location(new double[]{36.82, 10.19}).build();

        when(containerRepo.findAll()).thenReturn(List.of(c1, c2, c3, c4));

        // No PENDING tasks contain any of these → they are all "free"
        when(taskRepo.findAll()).thenReturn(List.of());

        Employee loader = Employee.builder().id("loader1").role(Role.loader_role).available(true).build();
        Employee driver = Employee.builder().id("driver1").role(Role.driver_role).available(true).build();
        when(employeeRepo.findByRoleAndAvailableTrue(Role.loader_role)).thenReturn(List.of(loader));
        when(employeeRepo.findByRoleAndAvailableTrue(Role.driver_role)).thenReturn(List.of(driver));
        when(employeeRepo.findById("loader1")).thenReturn(Optional.of(loader));
        when(employeeRepo.findById("driver1")).thenReturn(Optional.of(driver));

        Vehicle truck = Vehicle.builder()
                .id("truck-01")
                .capacity(10)
                .available(true)
                .location(new double[]{36.8065, 10.1815})
                .build();
        when(vehicleRepo.findByAvailableTrue()).thenReturn(List.of(truck));
        when(vehicleRepo.findById("truck-01")).thenReturn(Optional.of(truck));

        RouteSolution fakeSolution = RouteSolution.builder()
                .containerOrder(List.of("c2", "c4", "c3", "c1"))
                .encodedPolyline("qmr_Fghc}...")
                .totalDistanceKm(12.4)
                .totalDurationMin(28)
                .build();
        when(optimizer.optimizeRoute(anyList(), eq(truck))).thenReturn(fakeSolution);

        // ACT
        simulator.simulate();

        // ASSERT: Task created with all 4 containers
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepo).save(taskCaptor.capture());
        Task task = taskCaptor.getValue();
        assertEquals("Auto collection", task.getTitle());
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertEquals("truck-01", task.getVehiculeId());
        assertThat(task.getContainersIDs()).containsExactlyInAnyOrder("c1", "c2", "c3", "c4");

        // ASSERT: Route saved
        ArgumentCaptor<Route> routeCaptor = ArgumentCaptor.forClass(Route.class);
        verify(routeRepo).save(routeCaptor.capture());
        Route route = routeCaptor.getValue();
        assertEquals(task.getId(), route.getTaskId());
        assertThat(route.getRouteOrder()).containsExactly("c2", "c4", "c3", "c1");
        assertEquals(12.4, route.getTotalDistanceKm(), 0.01);

        // ASSERT: Availability blocked
        verify(employeeRepo, times(2)).save(any(Employee.class));
        verify(vehicleRepo).save(truck);
        verify(ws).convertAndSend("/topic/tasks", task);
    }

    @Test
    void simulate_doesNotCreateTask_whenLessThan4FreeContainers() {
        when(containerRepo.findAll()).thenReturn(List.of(
                Container.builder().id("c1").fillLevel(90).build(),
                Container.builder().id("c2").fillLevel(88).build(),
                Container.builder().id("c3").fillLevel(86).build()
        ));
        when(taskRepo.findAll()).thenReturn(List.of()); // all free

        simulator.simulate();

        verify(taskRepo, never()).save(any());
        verify(routeRepo, never()).save(any());
    }

}
