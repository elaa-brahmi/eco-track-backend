package com.example.demo.controller;
import com.example.demo.models.Report;
import com.example.demo.service.report.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    @PostMapping(value = "", consumes = "multipart/form-data")
    public Report create(
            @RequestPart("file") MultipartFile file,
            @RequestPart("description") String description,
            @RequestPart("location") String locationJson) {

        try {
            // Remove any whitespace just in case
            String cleanJson = locationJson.replaceAll("\\s", "");
            double[] location = new ObjectMapper().readValue(cleanJson, double[].class);
            return reportService.create(file, description, location);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid location format: " + locationJson);
        }
    }

    @GetMapping
    public List<Report> list() {
        return reportService.findAll();
    }

    @PutMapping("/{id}/resolve")
    public Report resolve(@PathVariable String id) {
        return reportService.resolve(id);
    }

    @GetMapping("/{id}")
    public Report getReport(@PathVariable String id) {
        return reportService.getReport(id);
    }
}
