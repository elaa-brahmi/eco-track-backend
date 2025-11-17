package com.example.demo.controller;
import com.example.demo.models.Report;
import com.example.demo.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public Report create(@RequestBody Report report) {
        return reportService.create(report);
    }

    @GetMapping
    public List<Report> list() {
        return reportService.findAll();
    }

    @PutMapping("/{id}/resolve")
    public Report resolve(@PathVariable String id) {
        return reportService.resolve(id);
    }
}
