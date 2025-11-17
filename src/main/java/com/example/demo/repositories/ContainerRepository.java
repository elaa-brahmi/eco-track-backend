package com.example.demo.repositories;

import com.example.demo.models.Container;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContainerRepository extends MongoRepository<Container, String> {
}