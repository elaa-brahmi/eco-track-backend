package com.example.demo.service.routing;
import com.example.demo.dto.RouteWithTaskDto;
import com.example.demo.models.Route;
import com.example.demo.models.Task;
import com.example.demo.models.TaskPriority;
import com.example.demo.models.TaskStatus;
import com.example.demo.repositories.RouteRepository;
import com.example.demo.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private TaskRepository taskRepo;

    @Mock
    private RouteRepository routeRepo;

    @InjectMocks
    private RouteService routeService;

    private Task task1, task2;
    private Route route1, route2;

    @BeforeEach
    void setUp() {
        // Task 1 — assigned to employee "emp-123"
        task1 = Task.builder()
                .id("task-001")
                .title("Morning Collection")
                .vehiculeId("truck-01")
                .employeesIDs(List.of("emp-123", "emp-456"))
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .dueDate(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .build();

        // Task 2 — also assigned to emp-123
        task2 = Task.builder()
                .id("task-002")
                .title("Afternoon Route")
                .vehiculeId("truck-02")
                .employeesIDs(List.of("emp-123"))
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .dueDate(Instant.now().plusSeconds(7200))
                .build();

        // Route for task-001
        route1 = Route.builder()
                .id("route-101")
                .taskId("task-001")
                .routeOrder(List.of("c12", "c8", "c3"))
                .polyline("qmr_Fghc}@HnBv@IaCse@XoB{@y@wA...")
                .totalDistanceKm(15.8)
                .totalDurationMin(38)
                .calculatedAt(Instant.parse("2025-04-05T08:30:00Z"))
                .build();

        // Route for task-002
        route2 = Route.builder()
                .id("route-102")
                .taskId("task-002")
                .routeOrder(List.of("c15", "c7"))
                .polyline("abcd_efgh...")
                .totalDistanceKm(9.2)
                .totalDurationMin(22)
                .calculatedAt(Instant.parse("2025-04-05T14:15:00Z"))
                .build();
    }

    @Test
    void getRoutesByEmployeeId_returnsRoutesForAssignedTasks_sortedByCalculatedAtDesc() {
        // GIVEN
        when(taskRepo.findByEmployeesIDsContaining("emp-123"))
                .thenReturn(List.of(task1, task2));

        when(routeRepo.findByTaskIdIn(List.of("task-001", "task-002")))
                .thenReturn(List.of(route1, route2));

        // WHEN
        List<RouteWithTaskDto> result = routeService.getRoutesByEmployeeId("emp-123");

        // THEN
        assertThat(result).hasSize(2);

        // Should be sorted: newest calculatedAt first → route2 (14:15) before route1 (08:30)
        assertThat(result.get(0).getRouteId()).isEqualTo("route-102");
        assertThat(result.get(1).getRouteId()).isEqualTo("route-101");

        RouteWithTaskDto first = result.get(0);
        assertThat(first.getTaskId()).isEqualTo("task-002");
        assertThat(first.getVehicleId()).isEqualTo("truck-02");
        assertThat(first.getContainerOrder()).containsExactly("c15", "c7");
        assertThat(first.getPolyline()).startsWith("abcd_");
        assertThat(first.getTotalDistanceKm()).isEqualTo(9.2);
        assertThat(first.getTotalDurationMin()).isEqualTo(22);

        RouteWithTaskDto second = result.get(1);
        assertThat(second.getRouteId()).isEqualTo("route-101");
    }

    @Test
    void getRoutesByEmployeeId_returnsEmptyList_whenNoTasksAssigned() {
        // GIVEN
        when(taskRepo.findByEmployeesIDsContaining("emp-999"))
                .thenReturn(List.of());

        // WHEN
        List<RouteWithTaskDto> result = routeService.getRoutesByEmployeeId("emp-999");

        // THEN
        assertThat(result).isEmpty();
        verify(routeRepo, never()).findByTaskIdIn(any());
    }

    @Test
    void getRoutesByEmployeeId_returnsOnlyRoutesWithExistingTask() {
        // GIVEN: employee has one task with route, one task without route
        Task taskWithoutRoute = Task.builder().id("task-003").employeesIDs(List.of("emp-123")).build();

        when(taskRepo.findByEmployeesIDsContaining("emp-123"))
                .thenReturn(List.of(task1, taskWithoutRoute));

        when(routeRepo.findByTaskIdIn(List.of("task-001", "task-003")))
                .thenReturn(List.of(route1)); // only one route exists

        // WHEN
        List<RouteWithTaskDto> result = routeService.getRoutesByEmployeeId("emp-123");

        // THEN
        assertThat(result).hasSize(1);
    }

}