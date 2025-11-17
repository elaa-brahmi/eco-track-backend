package com.example.demo;
import com.example.demo.models.Vehicle;
import com.example.demo.repositories.VehicleRepository;
import com.example.demo.service.vehicule.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class VehicleServiceImplTest {

    @Mock
    VehicleRepository repository;

    @InjectMocks
    VehicleServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreate() {
        Vehicle v = new Vehicle();
        when(repository.save(any())).thenReturn(v);
        assertNotNull(service.create(v));
    }

    @Test
    void testFindById() {
        Vehicle v = new Vehicle();
        v.setId("v1");

        when(repository.findById("v1")).thenReturn(Optional.of(v));

        Vehicle result = service.findById("v1");
        assertEquals("v1", result.getId());
    }
}
