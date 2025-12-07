package com.example.demo.controller;

import com.example.demo.config.SecurityConfig;
import com.example.demo.models.Vehicle;
import com.example.demo.service.vehicule.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehiculeController.class)
@Import(SecurityConfig.class)

class VehiculeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleService vehicleService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/vehicules";

    @Test
    void addVehicle() throws Exception {
        Vehicle input = Vehicle.builder()
                .name("Camion 01 - Tunis")
                .location(new double[]{10.185, 36.810})
                .capacity(5)
                .build();

        Vehicle saved = Vehicle.builder()
                .id("veh-001")
                .name("Camion 01 - Tunis")
                .location(new double[]{10.185, 36.810})
                .capacity(5)
                .build();

        when(vehicleService.create(any(Vehicle.class))).thenReturn(saved);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("veh-001"))
                .andExpect(jsonPath("$.name").value("Camion 01 - Tunis"))
                .andExpect(jsonPath("$.capacity").value(5))
                .andExpect(jsonPath("$.location[0]").value(10.185))
                .andExpect(jsonPath("$.location[1]").value(36.810));

        verify(vehicleService, times(1)).create(any());
    }

    @Test
    void getAllVehicles() throws Exception {
        Vehicle v1 = Vehicle.builder()
                .id("1")
                .name("Compacteur Sfax")
                .location(new double[]{10.766, 34.740})
                .capacity(2)
                .build();
        Vehicle v2 = Vehicle.builder()
                .id("2")
                .name("Petit Porteur Bizerte")
                .location(new double[]{9.863, 37.276})
                .capacity(8)
                .build();

        when(vehicleService.findAll()).thenReturn(List.of(v1, v2));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Compacteur Sfax"))
                .andExpect(jsonPath("$[0].location[0]").value(10.766))
                .andExpect(jsonPath("$[1].capacity").value(8));

        verify(vehicleService).findAll();
    }

    @Test
    void getVehicleById() throws Exception {
        Vehicle vehicle = Vehicle.builder()
                .id("veh-777")
                .name("Électrique La Marsa")
                .location(new double[]{10.330, 36.880})
                .capacity(12)
                .build();

        when(vehicleService.findById("veh-777")).thenReturn(vehicle);

        mockMvc.perform(get(BASE_URL + "/veh-777")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("veh-777"))
                .andExpect(jsonPath("$.name").value("Électrique La Marsa"))
                .andExpect(jsonPath("$.capacity").value(12))
                .andExpect(jsonPath("$.location[1]").value(36.880));

        verify(vehicleService).findById("veh-777");
    }

    @Test
    void updateVehicle() throws Exception {
        Vehicle updated = Vehicle.builder()
                .id("veh-555")
                .name("Camion 02 - Ariana")
                .location(new double[]{10.195, 36.862})
                .capacity(8)
                .build();

        when(vehicleService.update(eq("veh-555"), any(Vehicle.class))).thenReturn(updated);

        mockMvc.perform(put(BASE_URL + "/veh-555")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Camion 02 - Ariana"))
                .andExpect(jsonPath("$.location[0]").value(10.195))
                .andExpect(jsonPath("$.capacity").value(8));

        verify(vehicleService).update(eq("veh-555"), any());
    }

    @Test
    void deleteVehicle() throws Exception {
        doNothing().when(vehicleService).delete("veh-999");

        mockMvc.perform(delete(BASE_URL + "/veh-999")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))

                .andExpect(status().isNoContent());

        verify(vehicleService, times(1)).delete("veh-999");
    }
    @Test
    void getLocationByVehicleId() throws Exception {

        double[] location = {10.185, 36.810};

        Vehicle vehicle = Vehicle.builder()
                .id("veh-123")
                .name("Camion Test")
                .location(location)
                .capacity(5)
                .build();

        when(vehicleService.findById("veh-123")).thenReturn(vehicle);

        mockMvc.perform(get(BASE_URL + "/location/{id}", "veh-123")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(10.185))
                .andExpect(jsonPath("$[1]").value(36.810));

        verify(vehicleService).findById("veh-123");
    }

}