package com.example.demo.repositories;
import com.example.demo.models.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EmployeeRepository extends MongoRepository<Employee, String> {
    Optional<Employee> findByKeycloakId(String keycloakId);
    boolean existsByKeycloakId(String keycloakId);
    void deleteByKeycloakId(String keycloakId);
}

