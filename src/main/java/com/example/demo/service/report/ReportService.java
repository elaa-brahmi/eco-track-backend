package com.example.demo.service.report;

import com.example.demo.models.Report;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReportService {
    Report create(MultipartFile file, String description, String location);
    List<Report> findAll();
    Report resolve(String id);
}
