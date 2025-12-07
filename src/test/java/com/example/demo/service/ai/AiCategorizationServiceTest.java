package com.example.demo.service.ai;

import com.example.demo.models.ReportType;
import org.apache.http.HttpEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiCategorizationServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    AiCategorizationService aiCategorizationService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(aiCategorizationService, "GROQ_TOKEN", "xai-qKulGN82w4JsNKdiltYmGJw5DbuvJNd7JaBc9NyqpjcDWxWYkhm8ExvWtjMICaocxcx5BuN9x93y1yeD");
    }

    @Test
    void shouldReturnOrganic() {
        // Mock response structure
        Map<String, Object> message = Map.of("content", "Organic waste issue");
        Map<String, Object> choice = Map.of("message", message);
        Map<String, Object> body = Map.of("choices", List.of(choice));

        ResponseEntity<Map> response = new ResponseEntity<>(body, HttpStatus.OK);

        // CORRECT stubbing
        doReturn(response)
                .when(restTemplate)
                .postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));

        ReportType result = aiCategorizationService.categorize("there is rotten food overflowing");

        assertEquals(ReportType.Organic_waste_issue, result);
    }


    @Test
    void shouldFallbackToSanitationWhenGroqFails() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("API down"));

        ReportType result = aiCategorizationService.categorize("broken trash can");

        assertEquals(ReportType.Sanitation_problem, result);
    }
}
