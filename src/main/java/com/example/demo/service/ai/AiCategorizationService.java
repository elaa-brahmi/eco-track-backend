package com.example.demo.service.ai;

import com.example.demo.models.ReportType;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AiCategorizationService {

    @Value("${huggingface.api.key}")
    private String hfApiKey;

    private static final String HF_URL = "https://api-inference.huggingface.co/models/sentence-transformers/all-MiniLM-L6-v2";

    private static final List<String> LABELS = List.of(
            "Organic waste issue",
            "Overflow",
            "Sanitation problem"
    );

    public ReportType categorize(String text) {
        try {
            OkHttpClient client = new OkHttpClient();

            JSONObject payload = new JSONObject();
            payload.put("inputs", text);
            payload.put("parameters", new JSONObject()
                    .put("candidate_labels", LABELS)
            );

            RequestBody body = RequestBody.create(
                    payload.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(HF_URL)
                    .addHeader("Authorization", "Bearer " + hfApiKey)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            JSONObject json = new JSONObject(responseBody);

            String label = json.getJSONArray("labels").getString(0);

            return switch (label) {
                case "Organic waste issue" -> ReportType.Organic_waste_issue;
                case "Overflow" -> ReportType.Overflow;
                case "Sanitation problem" -> ReportType.Sanitation_problem;
                default -> ReportType.Sanitation_problem;
            };

        } catch (Exception e) {
            throw new RuntimeException("Failed to categorize report", e);
        }
    }
}
