package com.example.demo.utils;

import com.example.demo.dto.RouteSolution;
import com.example.demo.models.*;
import com.example.demo.repositories.*;
import com.example.demo.service.routing.RouteOptimizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class SensorSimulator {

    private final ContainerRepository repo;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmployeeRepository employeeRepo;
    private final VehicleRepository vehicleRepo;
    private final TaskRepository taskRepo;
    private final SimpMessagingTemplate ws;
    private final RouteOptimizationService optimizer;
    private final RouteRepository routeRepo;



    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void simulate() {
        System.out.println("Simulating");
        List<Container> containers = repo.findAll();

        // ---- INCREMENT FILL LEVEL ----
        for (Container c : containers) {
            int noise = new Random().nextInt(6);
            int newFill = Math.min(100, c.getFillLevel() + noise);

            c.setFillLevel(newFill);
            if (newFill >= 50) c.setStatus("half full");
            if (newFill >= 80) c.setStatus("alert");
            if (newFill == 100) c.setStatus("full");

            repo.save(c);
            messagingTemplate.convertAndSend("/topic/containers", c);
        }

        //  filter containers with  fill > 75

        //if one of these containers is already assigned to a task having status pending  , skip it
        List<Container> freeFullContainers = containers.stream()
                .filter(container -> container.getFillLevel() > 75)
                .filter(container -> {
                    // Is this container in any PENDING task?
                    return taskRepo.findAll().stream()
                            .noneMatch(task ->
                                    task.getStatus() == TaskStatus.PENDING &&
                                            task.getContainersIDs() != null &&
                                            task.getContainersIDs().contains(container.getId())
                            );
                })
                .sorted(Comparator.comparingInt(Container::getFillLevel).reversed()) // fullest first
                .toList();

        if (freeFullContainers.size() < 4) {
            System.out.println("Only " + freeFullContainers.size() +
                    " free full containers available (need 4). Skipping auto-task.");
            return;
        }





        // assign employees
        List<String> assignedEmployees = new ArrayList<>();
        assignedEmployees.addAll(employeeRepo.findByRoleAndAvailableTrue(Role.loader_role).stream()
                .limit(1)
                .map(Employee::getId)
                .toList());

        assignedEmployees.addAll(employeeRepo.findByRoleAndAvailableTrue(Role.driver_role).stream()
                .limit(1)
                .map(Employee::getId)
                .toList());

        if (assignedEmployees.size() < 2) {
            System.err.println("Not enough employees available to create task");
            return;
        }

        // assign vehicule
        List<Vehicle> availableVehicles = vehicleRepo.findByAvailableTrue();
        if (availableVehicles.isEmpty()) {
            System.err.println("No available vehicles for task");
            return;
        }

        Vehicle vehicle = availableVehicles.stream()
                .max(Comparator.comparingInt(Vehicle::getCapacity))
                .get();

        List<Container> assignedContainers;
        if (vehicle.getCapacity() >= freeFullContainers.size()) {
            assignedContainers = freeFullContainers;
        } else {
            assignedContainers = freeFullContainers.subList(0, vehicle.getCapacity());
        }

        // create task
        Task task = new Task();
        task.setTitle("Auto collection");
        task.setPriority(TaskPriority.MEDIUM);
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(Instant.now());
        task.setDueDate(Instant.now().plusSeconds(30 * 60));
        task.setContainersIDs(assignedContainers.stream().map(Container::getId).toList());
        task.setEmployeesIDs(assignedEmployees);
        task.setVehiculeId(vehicle.getId());

        taskRepo.save(task);

        //get the optimal route

        RouteSolution solution = optimizer.optimizeRoute(assignedContainers, vehicle);

        // create route

        Route route = new Route();
        route.setTaskId(task.getId());
        route.setVehicleId(vehicle.getId());
        route.setContainersIds(assignedContainers.stream().map(Container::getId).toList());
        route.setRouteOrder(solution.getContainerOrder());
        route.setPolyline(solution.getEncodedPolyline());
        route.setTotalDistanceKm(solution.getTotalDistanceKm());
        route.setTotalDurationMin(solution.getTotalDurationMin());
        route.setCalculatedAt(Instant.now());
        routeRepo.save(route);

        messagingTemplate.convertAndSend("/topic/tasks", task);

        //  block  availability
        Instant start = Instant.now();
        Instant end = Instant.now().plusSeconds(30 * 60);
        blockAvailability(assignedEmployees, vehicle.getId(), task.getId(), start, end);

        System.out.println("Task created with " + assignedContainers.size() + " containers, vehicle " + vehicle.getId());
    }

    private  void blockAvailability(List<String> employeeIds, String vehicleId, String taskId, Instant start, Instant end) {

        // Employees
        employeeIds.forEach(id -> {
            employeeRepo.findById(id).ifPresentOrElse(emp -> {
                emp.setAvailable(false);

                // Initialize schedule if null
                if (emp.getSchedule() == null) {
                    emp.setSchedule(new ArrayList<>());
                }

                emp.getSchedule().add(new AssignmentSlot(taskId, start, end));
                employeeRepo.save(emp);
            }, () -> {
                System.err.println("Warning: Employee not found: " + id);
            });
        });



        // Vehicle
        vehicleRepo.findById(vehicleId).ifPresentOrElse(vehicle -> {
            vehicle.setAvailable(false);
            if (vehicle.getSchedule() == null) {
                vehicle.setSchedule(new ArrayList<>());
            }
            vehicle.getSchedule().add(new AssignmentSlot(taskId, start, end));
            vehicleRepo.save(vehicle);
        }, () -> {
            throw new RuntimeException("Vehicle not found: " + vehicleId);
        });
    }

}
