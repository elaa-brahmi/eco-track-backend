package com.example.demo.service.report;
import com.example.demo.models.Report;
import com.example.demo.models.ReportStatus;
import com.example.demo.models.ReportType;
import com.example.demo.repositories.ReportRepository;
import com.example.demo.service.ai.AiCategorizationService;
import com.example.demo.service.report.ReportServiceImpl;
import com.example.demo.service.storage.SupabaseStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportRepository repo;

    @Mock
    private SupabaseStorageService storageService;
    @Mock
    private AiCategorizationService categorizationService;
    @Mock private SimpMessagingTemplate ws;

    @InjectMocks
    private ReportServiceImpl service;
    private Report existingReport;

    @BeforeEach
    void setUp() {
        existingReport = Report.builder()
                .id("report-123")
                .description("Garbage overflow in La Marsa")
                .location(new double[]{36.8181, 10.3254})
                .photoUrl("https://supabase.co/storage/old.jpg")
                .status(ReportStatus.NEW)
                .createdAt(Instant.now())
                .build();
    }



    @Test
    void testCreateReport() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getBytes()).thenReturn("fake-image-data".getBytes());



        when(storageService.uploadImage(any(), eq("jpg")))
                .thenReturn("https://supabase.co/storage/image.jpg");
        when(categorizationService.categorize("Overflowing bin in Tunis"))
                .thenReturn(ReportType.Overflow);


        when(repo.save(any(Report.class))).thenAnswer(i -> i.getArguments()[0]);

        Report result = service.create(file, "Overflowing bin in Tunis", new double[]{36.8065, 10.1815});

        assertEquals("https://supabase.co/storage/image.jpg", result.getPhotoUrl());
        assertEquals("Overflowing bin in Tunis", result.getDescription());
        assertEquals("NEW", result.getStatus().toString());
        assertEquals("Overflow",result.getType().toString());
        assertNotNull(result.getCreatedAt());

        // VERIFY WebSocket message was sent
        verify(ws).convertAndSend("/topic/reports", result);
    }
    @Test
    void findAll() {
        // GIVEN
        List<Report> reports = List.of(
                existingReport,
                Report.builder().id("report-456").description("Bin on fire").status(ReportStatus.NEW).build()
        );
        when(repo.findAll()).thenReturn(reports);

        List<Report> result = service.findAll();

        assertEquals(2, result.size());
        assertSame(reports, result);
        verify(repo).findAll();
        verifyNoInteractions(ws); // no broadcast on read
    }

    @Test
    void getReport() {
        // GIVEN
        when(repo.findById("report-123")).thenReturn(Optional.of(existingReport));

        // WHEN
        Report result = service.getReport("report-123");

        // THEN
        assertEquals("report-123", result.getId());
        assertEquals("Garbage overflow in La Marsa", result.getDescription());
        assertEquals("NEW", result.getStatus().toString());
        verify(repo).findById("report-123");
    }
    @Test
    void getReport_nonExistingId_shouldThrowException() {
        // GIVEN
        when(repo.findById("unknown")).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.getReport("unknown"));

        assertEquals("Report not found", exception.getMessage());
        verify(repo).findById("unknown");
    }
    @Test
    void resolve_existingReport() {
        when(repo.findById("report-123")).thenReturn(Optional.of(existingReport));
        when(repo.save(any(Report.class))).thenAnswer(i -> i.getArguments()[0]);

        Report resolved = service.resolve("report-123");

        assertEquals("RESOLVED", resolved.getStatus().toString());
        assertSame(existingReport, resolved);

        verify(repo).findById("report-123");
        verify(repo).save(existingReport);
    }

}
