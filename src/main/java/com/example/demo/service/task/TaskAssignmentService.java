package com.example.demo.service.task;

import com.example.demo.dto.ResolveReportRequest;
import com.example.demo.dto.ResolveReportResponse;
import com.example.demo.dto.RouteSolution;
import com.example.demo.models.*;
import com.example.demo.repositories.*;
import com.example.demo.service.routing.RouteOptimizationService;
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
    private final RouteOptimizationService optimizer;
    private final RouteRepository routeRepo;




    public ResolveReportResponse resolveReport(
            String reportId,
            ResolveReportRequest req
    ) {
        //  Load the report
        Report report = reportRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        //  Find nearest container to report
        Container container = containerRepo
                .findAll()
                .stream()
                .min(Comparator.comparingDouble(c ->
                        haversine(c.getLocation(), report.getLocation())))
                .orElseThrow(() -> new RuntimeException("No container found"));
        System.out.println(container);
        //if the container found belong to another task having status pending
        // return don't create a new task at all
        boolean isAssignedToPendingTask = taskRepo.findAll().stream()
                .anyMatch(task ->
                        task.getStatus() == TaskStatus.PENDING &&
                                task.getContainersIDs() != null &&
                                task.getContainersIDs().contains(container.getId())
                );


        if (isAssignedToPendingTask) {
            System.out.println("Container " + container.getId() +
                    " is already assigned to a PENDING task. Skipping new task creation.");
            throw new RuntimeException("container is already assigned to a PENDING task. Skipping new task creation.");
        }



            //  Find required employees
        List<String> assignedEmployeeIds = assignEmployees(req, container);

        //  Find nearest available vehicle
        Vehicle vehicle = assignVehicle(req, container);

        //  Create and save task
        Task task = new Task();
        task.setTitle(req.getTaskTitle());
        task.setPriority(req.getPriority());
        task.setStatus(TaskStatus.PENDING); //task is pending
        task.setReportId(reportId);
        task.setCreatedAt(Instant.now());
        task.setDueDate(req.getEnd());
        if (container != null) {
            task.setContainersIDs(List.of(container.getId()));
        } else {
            task.setContainersIDs(List.of()); // empty if none
        }
        task.setEmployeesIDs(assignedEmployeeIds);
        task.setVehiculeId(vehicle.getId());
        task = taskRepo.save(task);
        //build route
        assert container != null;
        RouteSolution solution = optimizer.optimizeRoute(List.of(container), vehicle);

        Route route = Route.builder()
                .taskId(task.getId())
                .routeOrder(solution.getContainerOrder())
                .polyline(solution.getEncodedPolyline())
                .totalDistanceKm(solution.getTotalDistanceKm())
                .totalDurationMin(solution.getTotalDurationMin())
                .calculatedAt(Instant.now())
                .build();
        routeRepo.save(route);
        if(!assignedEmployeeIds.isEmpty()) {
            ws.convertAndSend("/topic/tasks", task);
        }

        //  Block employee + vehicle availability based on time range
        blockAvailability(assignedEmployeeIds, vehicle.getId(), task.getId(), req.getStart(), req.getEnd());

        //  Update report status
        report.setStatus(ReportStatus.Under_Review);
        reportRepo.save(report);
        return new ResolveReportResponse(task.getId(), "task assigned");


    }

    // employee assignment
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


    // vehicle assignment
    private Vehicle assignVehicle(ResolveReportRequest req, Container container) {
        List<Vehicle> allVehicles = vehicleRepo.findAll(); // get all vehicles
        if (allVehicles.isEmpty()) {
            throw new RuntimeException("No vehicles exist in the system");
        }

        // Try to find nearest available vehicle
        return allVehicles.stream()
                .min(Comparator.comparingDouble(v ->
                        haversine(v.getLocation(), container != null ? container.getLocation() : new double[]{0,0})
                ))
                .get(); // safe because allVehicles is not empty
    }
    // availability block
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

    // haversine
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

        // ---- RESET CONTAINERS ----
        if (task.getContainersIDs() != null) {
            task.getContainersIDs().forEach(id -> {
                Container c = containerRepo.findById(id).orElse(null);
                if (c != null) {
                    c.setFillLevel(0);
                    c.setStatus("normal");
                    c.setLastEmptied(Instant.now());
                    containerRepo.save(c);
                }
            });
        }

        // ---- FREE EMPLOYEES ----
        if (task.getEmployeesIDs() != null) {
            task.getEmployeesIDs().forEach(empId -> {
                employeeRepo.findById(empId).ifPresent(emp -> {
                    emp.setAvailable(true);

                    // cleanup assignment slots for this task
                    if (emp.getSchedule() != null) {
                        emp.setSchedule(
                                emp.getSchedule().stream()
                                        .filter(slot -> !slot.getTaskId().equals(taskId))
                                        .toList()
                        );
                    }

                    employeeRepo.save(emp);
                });
            });
        }

        // ---- FREE VEHICLE ----
        if (task.getVehiculeId() != null) {
            vehicleRepo.findById(task.getVehiculeId()).ifPresent(vehicle -> {
                vehicle.setAvailable(true);

                if (vehicle.getSchedule() != null) {
                    vehicle.setSchedule(
                            vehicle.getSchedule().stream()
                                    .filter(slot -> !slot.getTaskId().equals(taskId))
                                    .toList()
                    );
                }

                vehicleRepo.save(vehicle);
            });
        }

        // ---- UPDATE TASK STATUS ----
        task.setStatus(TaskStatus.COMPLETED);
        taskRepo.save(task);

        // ---- UPDATE REPORT STATUS IF EXISTS ----
        if (task.getReportId() != null) {
            reportRepo.findById(task.getReportId()).ifPresent(report -> {
                report.setStatus(ReportStatus.RESOLVED);
                reportRepo.save(report);
            });
        }
    }


}

