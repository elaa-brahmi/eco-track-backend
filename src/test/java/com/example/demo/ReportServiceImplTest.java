package com.example.demo;
import com.example.demo.models.Report;
import com.example.demo.repositories.ReportRepository;
import com.example.demo.service.report.ReportServiceImpl;
import com.example.demo.service.storage.SupabaseStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ReportServiceImplTest {

    @Mock
    private ReportRepository repo;

    @Mock
    private SupabaseStorageService storageService;

    @InjectMocks
    private ReportServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateReport() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getBytes()).thenReturn("abc".getBytes());

        when(storageService.uploadImage(any(), eq("jpg")))
                .thenReturn("https://supabase/image.jpg");

        when(repo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Report result = service.create(file, "Overflowing", "10.1, 368");

        assertEquals("https://supabase/image.jpg", result.getPhotoUrl());
        assertEquals("Overflowing", result.getDescription());
        assertEquals("NEW", result.getStatus());
    }
}
