package com.example.demo.service.ai;

import com.example.demo.models.ReportType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;




@Service
public class AiCategorizationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Value("${grok.api.key}")
    private String GROQ_TOKEN ;

    private static final List<String> LABELS = List.of("Organic waste issue", "Overflow", "Sanitation problem");

    public ReportType categorize(String text) {
        if (text == null || text.trim().isEmpty()) return ReportType.Sanitation_problem;

        String prompt = "Classify this waste report into exactly one category from: Organic waste issue, Overflow, Sanitation problem. Reply with ONLY the category name. Report: " + text.trim();

        Map<String, Object> message = Map.of("role", "user", "content", prompt);
        Map<String, Object> payload = Map.of(
                "model", "llama-3.1-8b-instant",
                "messages", List.of(message),
                "max_tokens", 5,
                "temperature", 0.0
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(GROQ_TOKEN);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(GROQ_URL, request, Map.class);
            Map<String, Object> body = resp.getBody();
            if (body != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
                String result = ((String) msg.get("content")).trim();

                return switch (result) {
                    case "Organic waste issue" -> ReportType.Organic_waste_issue;
                    case "Overflow" -> ReportType.Overflow;
                    default -> ReportType.Sanitation_problem;
                };
            }
        } catch (Exception e) {
            System.err.println("Groq failed: " + e.getMessage());
        }

        return ReportType.Sanitation_problem;
    }
}