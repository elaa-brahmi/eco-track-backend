package com.example.demo.service.routing;

import com.example.demo.dto.RouteWithTaskDto;
import com.example.demo.models.Route;
import com.example.demo.models.Task;
import com.example.demo.repositories.RouteRepository;
import com.example.demo.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Comparator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final RouteRepository routeRepository;

    private final TaskRepository taskRepo;
    private final RouteRepository routeRepo;

    public List<RouteWithTaskDto> getRoutesByEmployeeId(String employeeId) {

        //  Find all tasks assigned to this employee
        List<Task> tasks = taskRepo.findByEmployeesIDsContaining(employeeId);

        if (tasks.isEmpty()) {
            return List.of();
        }

        //  Extract task IDs
        List<String> taskIds = tasks.stream()
                .map(Task::getId)
                .toList();

        //  Find all routes for these tasks
        List<Route> routes = routeRepo.findByTaskIdIn(taskIds);

        //  Combine Route + Task data
        return routes.stream()
                .map(route -> {
                    Task task = tasks.stream()
                            .filter(t -> t.getId().equals(route.getTaskId()))
                            .findFirst()
                            .orElse(null);

                    return new RouteWithTaskDto(
                            route.getId(),
                            route.getTaskId(),
                            route.getVehicleId(),
                            route.getContainersIds(),
                            route.getRouteOrder(),
                            route.getPolyline(),
                            route.getTotalDistanceKm(),
                            route.getTotalDurationMin(),
                            route.getCalculatedAt()
                    );
                })
                .sorted(Comparator.comparing(RouteWithTaskDto::getCalculatedAt).reversed()) // newest first
                .toList();
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

}
