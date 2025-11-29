package com.example.demo.service.vehicule;

import com.example.demo.models.Vehicle;
import com.example.demo.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository repository;

    @Override
    public List<Vehicle> findAll() {
        return repository.findAll();
    }

    @Override
    public Vehicle findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
    }

    @Override
    public Vehicle create(Vehicle vehicle) {
        vehicle.setAvailable(true);
        return repository.save(vehicle);
    }

    @Override
    public Vehicle update(String id, Vehicle updated) {
        Vehicle existing = findById(id);
        existing.setName(updated.getName());
        existing.setLocation(updated.getLocation());
        existing.setCapacity(updated.getCapacity());
        return repository.save(existing);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }
}