package com.example.demo.controller;

import com.example.demo.config.SecurityConfig;
import com.example.demo.models.Container;
import com.example.demo.service.container.ContainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;  // ← Crucial: Static imports for get/post/etc.
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;  // ← Static imports for status/jsonPath/etc.

@WebMvcTest(ContainerController.class)
@Import(SecurityConfig.class)

class ContainerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContainerService containerService;

    @Autowired
    private ObjectMapper objectMapper;  // For JSON conversion

    private final String BASE_URL = "/api/containers";


    @Test
    void shouldReturnAllContainers() throws Exception {
        Container c1 = Container.builder().id("1").type("plastic").fillLevel(40).build();
        Container c2 = Container.builder().id("2").type("glass").fillLevel(80).build();

        when(containerService.findAll()).thenReturn(List.of(c1, c2));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].type").value("glass"));

        verify(containerService).findAll();
    }

    @Test
    void getById()  throws Exception {
        Container c1 = Container.builder().id("1").type("plastic").build();
        when(containerService.findById("1")).thenReturn(c1);
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/{id}", 1)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.type").value("plastic"));
    }

    @Test
    void shouldUpdateContainer() throws Exception {
        Container updated = Container.builder().fillLevel(90).status("full").build();

        when(containerService.update(eq("500"), any())).thenReturn(updated);

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/500")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))

                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.fillLevel").value(90));

        verify(containerService).update(eq("500"), any());
    }

    @Test
    void shouldDeleteContainer() throws Exception {
        doNothing().when(containerService).delete("777");

        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/777")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))

                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(containerService).delete("777");
    }
    @Test
    void getLocationByContainerId() throws Exception {
        double[] location = {10, 20};

        Container c1 = Container.builder()
                .id("1")
                .type("plastic")
                .location(location)
                .build();

        when(containerService.findById("1")).thenReturn(c1);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/location/{id}", "1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(10.0))
                .andExpect(jsonPath("$[1]").value(20.0));

        verify(containerService).findById("1");

    }
}