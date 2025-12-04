package com.example.demo.controller;


import com.example.demo.config.SecurityConfig;
import com.example.demo.dto.RouteWithTaskDto;
import com.example.demo.models.Route;
import com.example.demo.service.routing.RouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouteController.class)
@Import(SecurityConfig.class)

class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RouteService routeService;

    private Route route1, route2;
    private RouteWithTaskDto dto1, dto2; // Now proper fields

    @BeforeEach
    void setUp() {
        route1 = Route.builder()
                .id("route-001")
                .taskId("task-001")
                .polyline("abcd...")
                .totalDistanceKm(12.5)
                .totalDurationMin(30)
                .calculatedAt(Instant.now())
                .build();

        route2 = Route.builder()
                .id("route-002")
                .taskId("task-002")
                .polyline("xyz...")
                .totalDistanceKm(8.1)
                .totalDurationMin(19)
                .calculatedAt(Instant.now().minusSeconds(3600))
                .build();

        // FIXED: Correct constructor order + assigned to fields
        dto1 = new RouteWithTaskDto(
                "route-001",
                "task-001",
                "truck-01",
                List.of("c1", "c2"),
                List.of("c1", "c2"),

                "abcd...",
                12.5,
                30.0,
                Instant.now().plusSeconds(3600)
        );

        dto2 = new RouteWithTaskDto(
                "route-002",
                "task-002",
                "truck-02",
                List.of("c3", "c4"),
                List.of("c3", "c4"),

                "xyz...",
                8.1,
                19.0,
                Instant.now().plusSeconds(7200)
        );
    }

    @Test
    void getRoutes_returns200_withAllRoutes() throws Exception {
        when(routeService.getAllRoutes()).thenReturn(List.of(route1, route2));

        mockMvc.perform(get("/api/route"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("route-001"))
                .andExpect(jsonPath("$[0].totalDistanceKm").value(12.5))
                .andExpect(jsonPath("$[1].id").value("route-002"));

        verify(routeService).getAllRoutes();
    }

    @Test
    void getRoutes_returnsEmptyList_whenNoRoutes() throws Exception {
        when(routeService.getAllRoutes()).thenReturn(List.of());

        mockMvc.perform(get("/api/route"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(routeService).getAllRoutes();
    }

    @Test
    void getRoute_byEmployeeId_returns200_withEmployeeRoutes() throws Exception {
        when(routeService.getRoutesByEmployeeId("emp-123"))
                .thenReturn(List.of(dto2, dto1));

        mockMvc.perform(get("/api/route/emp-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].routeId").value("route-002"))
                .andExpect(jsonPath("$[0].vehicleId").value("truck-02"))
                .andExpect(jsonPath("$[0].containerOrder[0]").value("c3"))
                .andExpect(jsonPath("$[0].totalDistanceKm").value(8.1))
                .andExpect(jsonPath("$[1].routeId").value("route-001"))
                .andExpect(jsonPath("$[1].containerOrder[1]").value("c2"))
                .andExpect(jsonPath("$[1].polyline").value("abcd..."));

        verify(routeService).getRoutesByEmployeeId("emp-123");
    }

    @Test
    void getRoute_byEmployeeId_returnsEmptyList_whenNoRoutes() throws Exception {
        when(routeService.getRoutesByEmployeeId("emp-999"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/route/emp-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(routeService).getRoutesByEmployeeId("emp-999");
    }

    @Test
    void getRoute_invalidEmployeeId_stillReturns200_withEmptyList() throws Exception {
        when(routeService.getRoutesByEmployeeId("unknown"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/route/unknown"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}