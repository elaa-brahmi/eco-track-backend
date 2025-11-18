package com.example.demo.service.vehicule;
import com.example.demo.models.Vehicle;
import com.example.demo.repositories.VehicleRepository;
import com.example.demo.service.vehicule.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    VehicleRepository repository;

    @InjectMocks
    VehicleServiceImpl service;

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
