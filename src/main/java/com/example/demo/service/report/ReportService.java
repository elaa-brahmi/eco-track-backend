package com.example.demo.service.report;

import com.example.demo.models.Report;

import java.util.List;

public interface ReportService {
    Report create(Report r);
    List<Report> findAll();
    Report resolve(String id);
}
