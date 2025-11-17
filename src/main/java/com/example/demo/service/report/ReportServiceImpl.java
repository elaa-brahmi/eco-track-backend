package com.example.demo.service.report;

import com.example.demo.models.Report;
import com.example.demo.repositories.ReportRepository;
import com.example.demo.service.storage.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository repo;
    private final SupabaseStorageService storageService;


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
        Report report = Report.builder()
                .description(description)
                .location(location)
                .photoUrl(imageUrl)
                .createdAt(Instant.now())
                .status("NEW")
                .build();

        return repo.save(report);
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

