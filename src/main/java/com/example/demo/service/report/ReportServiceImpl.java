package com.example.demo.service.report;

import com.example.demo.models.Report;
import com.example.demo.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository repo;

    @Override
    public Report create(Report r) {
        r.setCreatedAt(Instant.now());
        r.setStatus("NEW");
        return repo.save(r);
    }

    @Override
    public List<Report> findAll() {
        return repo.findAll();
    }

    @Override
    public Report resolve(String id) {
        Report r = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        r.setStatus("RESOLVED");
        return repo.save(r);
    }
}

