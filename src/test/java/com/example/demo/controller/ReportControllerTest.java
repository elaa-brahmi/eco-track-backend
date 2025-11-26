package com.example.demo.controller;

import com.example.demo.config.SecurityConfig;
import com.example.demo.models.Report;
import com.example.demo.models.ReportStatus;
import com.example.demo.service.report.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)

class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createReport() throws Exception {
        Report savedReport = Report.builder()
                .id("report-123")
                .description("Garbage overflow at Soukra")
                .location(new double[]{36.8065, 10.1815})
                .photoUrl("https://supabase.co/storage/photo.jpg")
                .status(ReportStatus.NEW)
                .createdAt(Instant.now())
                .build();

        when(reportService.create(any(), eq("Garbage overflow at Soukra"), eq(new double[]{36.8065, 10.1815})))
                .thenReturn(savedReport);

        // CORRECT JSON — NO SPACES AFTER COMMA!
        String locationJson = "[36.8065,10.1815]";

        mockMvc.perform(multipart("/api/reports")
                        .file(new MockMultipartFile("file", "garbage.jpg", "image/jpeg", new byte[]{1,2,3,4}))
                        .file(new MockMultipartFile("description", "", "text/plain",
                                "Garbage overflow at Soukra".getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile("location", "", "application/json",
                                locationJson.getBytes(StandardCharsets.UTF_8)))  // ← PERFECT JSON
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_citizen-role"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("report-123"))
                .andExpect(jsonPath("$.description").value("Garbage overflow at Soukra"))
                .andExpect(jsonPath("$.location[0]").value(36.8065))
                .andExpect(jsonPath("$.location[1]").value(10.1815));
    }


    @Test
    void ListReports() throws Exception {
        when(reportService.findAll()).thenReturn(List.of(new Report()));

        mockMvc.perform(get("/api/reports")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_citizen-role"),
                                new SimpleGrantedAuthority("ROLE_admin-role")
                        )))
                .andExpect(status().isOk());
    }

    @Test
    void ResolveReport() throws Exception {
        Report resolved = new Report();
        resolved.setStatus(ReportStatus.RESOLVED);

        when(reportService.resolve("123")).thenReturn(resolved);

        mockMvc.perform(put("/api/reports/123/resolve")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin-role"))))
                .andExpect(status().isOk());
    }


    @Test
    void shouldDenyAccess_withoutToken() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isUnauthorized());
    }
}