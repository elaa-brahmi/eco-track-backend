package com.example.demo.utils;

import com.example.demo.models.*;
import com.example.demo.repositories.ContainerRepository;
import com.example.demo.repositories.EmployeeRepository;
import com.example.demo.repositories.TaskRepository;
import com.example.demo.repositories.VehicleRepository;
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
    private final ContainerRepository containerRepo;
    private final EmployeeRepository employeeRepo;
    private final VehicleRepository vehicleRepo;
    private final TaskRepository taskRepo;
    private final SimpMessagingTemplate ws;


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
            if (newFill >= 90) c.setStatus("alert");
            if (newFill == 100) c.setStatus("full");

            repo.save(c);
            messagingTemplate.convertAndSend("/topic/containers", c);
        }

        // ---- FILTER CONTAINERS WITH FILL > 75 ----
        List<Container> highFillContainers = containers.stream()
                .filter(container -> container.getFillLevel() > 75)
                .sorted((a, b) -> Integer.compare(b.getFillLevel(), a.getFillLevel())) // highest fill first
                .toList();

        if (highFillContainers.size() < 4) {
            System.out.println("Not enough high-fill containers to create a task (need at least 4).");
            return;
        }

        System.out.println("High fill containers: " + highFillContainers.size() + ", creating a new task automatically");

        // ---- ASSIGN EMPLOYEES ----
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

        // ---- ASSIGN VEHICLE ----
        List<Vehicle> availableVehicles = vehicleRepo.findByAvailableTrue();
        if (availableVehicles.isEmpty()) {
            System.err.println("No available vehicles for task");
            return;
        }

        Vehicle vehicle = availableVehicles.stream()
                .max(Comparator.comparingInt(Vehicle::getCapacity))
                .get();

        List<Container> assignedContainers;
        if (vehicle.getCapacity() >= highFillContainers.size()) {
            assignedContainers = highFillContainers;
        } else {
            assignedContainers = highFillContainers.subList(0, vehicle.getCapacity());
        }

        // ---- CREATE TASK ----
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
        messagingTemplate.convertAndSend("/topic/tasks", task);

        // ---- BLOCK AVAILABILITY ----
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
