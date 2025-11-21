package com.example.demo.controller;

import com.example.demo.models.Report;
import com.example.demo.service.report.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/reports";

    @Test
    void createReport() throws Exception {

        MockMultipartFile image = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "test image".getBytes());

        MockPart description = new MockPart("description", "Bin overflowing".getBytes());
        MockPart location = new MockPart("location", "36.8800,10.3300".getBytes());

        Report saved = Report.builder()
                .id("1")
                .description("Bin overflowing")
                .location(new double[]{10.3300, 36.8800})
                .photoUrl("https://supabase.co/storage/xyz.jpg")
                .status("NEW")
                .createdAt(Instant.now())
                .build();

        when(reportService.create(
                any(),
                eq("Bin overflowing"),
                eq(new double[]{36.8800, 10.3300})
        )).thenReturn(saved);

        mockMvc.perform(multipart("/api/reports")
                        .file(image)
                        .part(description)
                        .part(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.status").value("NEW"));

        verify(reportService).create(any(), anyString(), any(double[].class));
    }

    @Test
    void listReports() throws Exception {
        Report r1 = Report.builder().id("1").description("Full bin").status("NEW").build();
        Report r2 = Report.builder().id("2").description("Broken bin").status("RESOLVED").build();

        when(reportService.findAll()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].status").value("RESOLVED"));

        verify(reportService).findAll();
    }

    @Test
    void getReport() throws Exception {
        Report r1 = Report.builder().id("1").description("Full bin").status("NEW").build();
        when(reportService.getReport(anyString())).thenReturn(r1);

        mockMvc.perform(get(BASE_URL + "/" + r1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.description").value("Full bin"))
                .andExpect(jsonPath("$.status").value("NEW"));

        verify(reportService).getReport("1");
    }

    @Test
    void resolveReport() throws Exception {
        Report resolved = Report.builder()
                .id("55")
                .description("Already fixed")
                .status("RESOLVED")
                .build();

        when(reportService.resolve("55")).thenReturn(resolved);

        mockMvc.perform(put(BASE_URL + "/{id}/resolve", "55"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("55"))
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        verify(reportService).resolve("55");
    }

    @Test
    void createReport_withoutImage() throws Exception {

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "", "image/jpeg", new byte[0]);

        MockPart description = new MockPart("description", "No photo but urgent".getBytes());
        MockPart location = new MockPart("location", "36.862,10.195".getBytes());

        Report saved = Report.builder()
                .id("rep-002")
                .description("No photo but urgent")
                .location(new double[]{10.195, 36.862})
                .status("NEW")
                .createdAt(Instant.now())
                .build();

        when(reportService.create(
                any(),
                eq("No photo but urgent"),
                eq(new double[]{36.862, 10.195})
        )).thenReturn(saved);

        mockMvc.perform(multipart(BASE_URL)
                        .file(emptyFile)
                        .part(description)
                        .part(location))
                .andExpect(status().isOk());

        verify(reportService).create(any(), anyString(), any(double[].class));
    }
}
