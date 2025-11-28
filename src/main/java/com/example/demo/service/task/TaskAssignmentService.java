package com.example.demo.service.task;

import com.example.demo.dto.ResolveReportRequest;
import com.example.demo.dto.ResolveReportResponse;
import com.example.demo.models.*;
import com.example.demo.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskAssignmentService {

    private final ReportRepository reportRepo;
    private final ContainerRepository containerRepo;
    private final EmployeeRepository employeeRepo;
    private final VehicleRepository vehicleRepo;
    private final TaskRepository taskRepo;
    private final SimpMessagingTemplate ws;


    public ResolveReportResponse resolveReport(
            String reportId,
            ResolveReportRequest req
    ) {
        // 1. Load the report
        Report report = reportRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        // 2. Find nearest container to report
        Container container = containerRepo
                .findAll()
                .stream()
                .min(Comparator.comparingDouble(c ->
                        haversine(c.getLocation(), report.getLocation())))
                .orElseThrow(() -> new RuntimeException("No container found"));
        System.out.println(container);

        // 3. Find required employees
        List<String> assignedEmployeeIds = assignEmployees(req, container);

        // 4. Find nearest available vehicle
        Vehicle vehicle = assignVehicle(req, container);

        // 5. Create and save task
        Task task = new Task();
        task.setTitle(req.getTaskTitle());
        task.setPriority(req.getPriority());
        task.setStatus(TaskStatus.PENDING); //task is pending
        task.setReportId(reportId);
        task.setCreatedAt(Instant.now());
        task.setDueDate(req.getEnd());
        task.setContainersIDs(List.of(container.getId()));
        task.setEmployeesIDs(assignedEmployeeIds);
        task.setVehiculeId(vehicle.getId());

        task = taskRepo.save(task);
        if(!assignedEmployeeIds.isEmpty()) {
            ws.convertAndSend("/topic/tasks", task);
        }

        // 6. Block employee + vehicle availability based on time range
        blockAvailability(assignedEmployeeIds, vehicle.getId(), task.getId(), req.getStart(), req.getEnd());

        // 7. Update report status
        report.setStatus(ReportStatus.Under_Review);
        reportRepo.save(report);
        return new ResolveReportResponse(task.getId(), "task assigned");


    }

    // ---- EMPLOYEE ASSIGNMENT ----
    private List<String> assignEmployees(ResolveReportRequest req, Container container) {

        List<String> result = new ArrayList<>();

        result.addAll(findClosestAvailable(Role.collector_role,    req.getRequirement().getCollectors(),  container));
        result.addAll(findClosestAvailable(Role.loader_role,       req.getRequirement().getLoaders(),     container));
        result.addAll(findClosestAvailable(Role.driver_role,       req.getRequirement().getDrivers(),     container));
        result.addAll(findClosestAvailable(Role.maintenance_role,  req.getRequirement().getMaintenance(), container));

        return result;
    }

    private List<String> findClosestAvailable(Role role, int count, Container container) {
        if (count == 0) return List.of();

        return employeeRepo.findByRoleAndAvailableTrue(role).stream()
                .limit(count)
                .map(Employee::getId)
                .toList();
    }


    // ---- VEHICLE ASSIGNMENT ----
    private Vehicle assignVehicle(ResolveReportRequest req, Container container) {
        return vehicleRepo.findByAvailableTrue().stream()
                .sorted(Comparator.comparingDouble(v ->
                        haversine(v.getLocation(), container.getLocation())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No available vehicles"));
    }

    // ---- AVAILABILITY BLOCK ----
    private void blockAvailability(List<String> employeeIds, String vehicleId, String taskId, Instant start, Instant end) {

        // Employees
        employeeIds.forEach(id -> {
            employeeRepo.findById(id).ifPresentOrElse(emp -> {
                emp.setAvailable(false);
                emp.getSchedule().add(new AssignmentSlot(taskId, start, end));
                employeeRepo.save(emp);
            }, () -> {
                System.err.println("Warning: Employee not found: " + id);
            });
        });


        // Vehicle
        vehicleRepo.findById(vehicleId).ifPresentOrElse(vehicle -> {
            vehicle.setAvailable(false);
            vehicle.getSchedule().add(new AssignmentSlot(taskId, start, end));
            vehicleRepo.save(vehicle);
        }, () -> {
            throw new RuntimeException("Vehicle not found: " + vehicleId);
        });
    }

    // ---- HAVERSINE ----
    private double haversine(double[] p1, double[] p2) {
        double R = 6371;
        double dLat = Math.toRadians(p2[0] - p1[0]);
        double dLon = Math.toRadians(p2[1] - p1[1]);
        double lat1 = Math.toRadians(p1[0]);
        double lat2 = Math.toRadians(p2[0]);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);

        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public void completeTask(String taskId) {

        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Free employees
        task.getEmployeesIDs().forEach(empId -> {
            Employee emp = employeeRepo.findById(empId).get();
            emp.setAvailable(true);

            // remove or cleanup assignment slots
            emp.setSchedule(
                    emp.getSchedule().stream()
                            .filter(slot -> !slot.getTaskId().equals(taskId))
                            .toList()
            );

            employeeRepo.save(emp);
        });

        // Free vehicle
        Vehicle vehicle = vehicleRepo.findById(task.getVehiculeId()).get();
        vehicle.setAvailable(true);

        vehicle.setSchedule(
                vehicle.getSchedule().stream()
                        .filter(slot -> !slot.getTaskId().equals(taskId))
                        .toList()
        );

        vehicleRepo.save(vehicle);

        // Update task status
        if(task.getReportId() != null) {
            Report report = reportRepo.findById(task.getReportId()).get();
            report.setStatus(ReportStatus.RESOLVED);
            reportRepo.save(report);
        }
        task.setStatus(TaskStatus.COMPLETED);
        taskRepo.save(task);
    }

}

