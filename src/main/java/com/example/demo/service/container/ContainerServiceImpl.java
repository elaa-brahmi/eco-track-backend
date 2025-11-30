package com.example.demo.service.container;

import com.example.demo.dto.ContainerDto;
import com.example.demo.models.Container;
import com.example.demo.repositories.ContainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerServiceImpl implements ContainerService {

    private final ContainerRepository repository;

    @Override
    public List<Container> findAll() {
        return repository.findAll();
    }

    @Override
    public Container findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Container not found"));
    }

    @Override
    public Container create(ContainerDto c) {
        Container container = new Container();
        container.setType(c.getType());
        container.setLocation(c.getLocation());
        container.setLastUpdated(Instant.now());
        container.setStatus(c.getStatus());
        return repository.save(container);
    }

    @Override
    public Container update(String id, Container c) {
        Container existing = findById(id);
        existing.setFillLevel(c.getFillLevel());
        existing.setLocation(c.getLocation());
        existing.setType(c.getType());
        existing.setStatus(c.getStatus());
        existing.setLastUpdated(Instant.now());
        return repository.save(existing);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }
}
