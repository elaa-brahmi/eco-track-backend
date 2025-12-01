package com.example.demo.repositories;

import com.example.demo.models.Container;
import com.example.demo.models.Route;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RouteRepository extends MongoRepository<Route, String> {
    List<Route> findByTaskIdIn(List<String> taskIds);
}
