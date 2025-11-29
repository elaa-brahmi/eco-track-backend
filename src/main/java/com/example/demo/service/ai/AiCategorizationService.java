package com.example.demo.service.ai;

import com.example.demo.models.ReportType;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AiCategorizationService {

    @Value("${grok.api.key}")
    private String grokApiKey;

    private static final String GROK_URL = "https://api.x.ai/v1/chat/completions";  // Official Grok endpoint

    private static final List<String> LABELS = List.of(
            "Organic waste issue",
            "Overflow",
            "Sanitation problem"
    );

    public ReportType categorize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return ReportType.Sanitation_problem; // fallback
        }

        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            // Bulletproof prompt for zero-shot classification (tested to work)
            String prompt = "Classify this waste report into exactly one category from: Organic waste issue, Overflow, Sanitation problem. Reply with ONLY the category name. Report: " + text.trim();

            JSONObject payload = new JSONObject();
            payload.put("model", "grok-3-mini");  // Free tier model (stable, no 400 errors)
            payload.put("messages", new JSONArray().put(new JSONObject()
                    .put("role", "user")
                    .put("content", prompt)
            ));
            payload.put("max_tokens", 5);  // Even shorter — just the word
            payload.put("temperature", 0.0);  // Zero for 100% deterministic output

            RequestBody body = RequestBody.create(
                    payload.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(GROK_URL)
                    .addHeader("Authorization", "Bearer " + grokApiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    throw new RuntimeException("Grok API error: " + response.code() + " - " + errorBody);
                }

                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);

                // Grok returns: {"choices": [{"message": {"content": "Overflow"}}]}
                JSONArray choices = json.getJSONArray("choices");
                String category = choices.getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim();

                return switch (category) {
                    case "Organic waste issue" -> ReportType.Organic_waste_issue;
                    case "Overflow" -> ReportType.Overflow;
                    case "Sanitation problem" -> ReportType.Sanitation_problem;
                    default -> ReportType.Sanitation_problem;
                };
            }

        } catch (Exception e) {
            System.err.println("Grok categorization failed, using fallback: " + e.getMessage());
            return ReportType.Sanitation_problem; // fallback
        }
    }
}