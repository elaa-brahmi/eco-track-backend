package com.example.demo.service.task;

import com.example.demo.dto.TaskRequest;
import com.example.demo.dto.UpdateTaskRequest;
import com.example.demo.models.*;
import com.example.demo.repositories.ContainerRepository;
import com.example.demo.repositories.EmployeeRepository;
import com.example.demo.repositories.TaskRepository;
import com.example.demo.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final SimpMessagingTemplate ws;


    private final TaskRepository repository;
    private final EmployeeRepository employeeRepository;
    private final VehicleRepository vehicleRepository;
    private final ContainerRepository containerRepository;

    @Override
    public List<Task> findAll() {
        return repository.findAll();
    }

    @Override
    public Task findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }



    @Override
    public Task assign( TaskRequest request) {
        if(request.getEmployeesIDs()!=null && !request.getEmployeesIDs().isEmpty()) {
            for(String id : request.getEmployeesIDs()) {
                Employee emp=employeeRepository.findById(id).get();
                if(!emp.isAvailable()){
                    throw new RuntimeException("Employee not available");

                }
                emp.setAvailable(false);
            }

        }
        if(request.getVehiculeId()!=null && !request.getVehiculeId().isEmpty()) {
            Vehicle vehicle=vehicleRepository.findById(request.getVehiculeId()).get();
            if(!vehicle.isAvailable()){
                throw new RuntimeException("Vehicle not available");
            }
            vehicle.setAvailable(false);

        }
        Task task = new Task();
        task.setCreatedAt(Instant.now());
        task.setStatus(TaskStatus.valueOf(request.getStatus()));
        task.setPriority(TaskPriority.valueOf(request.getPriority()));
        task.setTitle(request.getTitle());
        task.setVehiculeId(request.getVehiculeId());
        task.setReportId(request.getReportId());
        task.setContainersIDs(request.getContainersIDs());
        task.setEmployeesIDs(request.getEmployeesIDs());
        ws.convertAndSend("/topic/tasks", task);
        return repository.save(task);
    }

    @Override
    public Task updateStatus(String taskId, UpdateTaskRequest request) {
        Task task = findById(taskId);

        // Only update fields that are present in the request
        task.setStatus(TaskStatus.valueOf(request.status()));
        return repository.save(task);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }
}