package com.example.demo.service.container;

import com.example.demo.models.Container;

import java.util.List;

public interface ContainerService {
    List<Container> findAll();
    Container findById(String id);
    Container create(Container c);
    Container update(String id, Container c);
    void delete(String id);
}