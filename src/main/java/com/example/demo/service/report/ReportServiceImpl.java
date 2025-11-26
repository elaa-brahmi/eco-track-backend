package com.example.demo.service.report;

import com.example.demo.models.Report;
import com.example.demo.models.ReportStatus;
import com.example.demo.models.ReportType;
import com.example.demo.repositories.ReportRepository;
import com.example.demo.service.ai.AiCategorizationService;
import com.example.demo.service.storage.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final SimpMessagingTemplate ws;
    private final ReportRepository repo;
    private final SupabaseStorageService storageService;
    private final AiCategorizationService categorizationService;


    @Override
    public Report create(MultipartFile file, String description, double[] location) {
        //save image to supabase
        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            String originalName = file.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf(".") + 1);
            try {
                imageUrl = storageService.uploadImage(file.getBytes(), ext);
            } catch (Exception e) {
                throw new RuntimeException("Image upload failed", e);
            }
        }
        //use categorize ai to categorize the report depending on description
        try {
            ReportType type = categorizationService.categorize(description);
            Report report = Report.builder()
                    .description(description)
                    .location(location)
                    .type(type)
                    .photoUrl(imageUrl)
                    .createdAt(Instant.now())
                    .status(ReportStatus.NEW)
                    .build();
            // get the report instantly on the dashboard.
            ws.convertAndSend("/topic/reports", report);

            return repo.save(report);
        }
        catch (Exception e) {
            throw new RuntimeException("Categorization failed", e);

        }


    }

    @Override
    public List<Report> findAll() {
        return repo.findAll();
    }

    @Override
    public Report resolve(String id) {
        Report r = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        r.setStatus(ReportStatus.RESOLVED);
        return repo.save(r);
    }
    @Override
    public Report getReport(String id) {
        Report r=repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        return r;
    }
}

