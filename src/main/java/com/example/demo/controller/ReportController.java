package com.example.demo.controller;
import com.example.demo.models.Report;
import com.example.demo.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    //@RequestPart (for multipart values)
    @PostMapping(value="", consumes= "multipart/form-data")
    public Report create(@RequestPart("file") MultipartFile file,
                         @RequestPart("description") String description,
                         @RequestPart("location") double[] location) {
        return reportService.create(file, description, location);
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
