package com.example.demo.controller;

import com.example.demo.dto.ContainerDto;
import com.example.demo.models.Container;
import com.example.demo.service.container.ContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/containers")
@RequiredArgsConstructor
public class ContainerController {

    private final ContainerService containerService;

    @GetMapping
    public List<Container> list() {
        return containerService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Container> get(@PathVariable String id) {
        return ResponseEntity.status(200).body(containerService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Container> create(@RequestBody ContainerDto c) {
        return ResponseEntity.status(201).body(containerService.create(c));
    }

    @PutMapping("/{id}")
    public Container update(@PathVariable String id, @RequestBody Container c) {
        return containerService.update(id, c);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        containerService.delete(id);
    }
}
