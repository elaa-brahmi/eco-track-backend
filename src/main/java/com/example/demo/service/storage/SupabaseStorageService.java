package com.example.demo.service.storage;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String SUPABASE_URL;

    @Value("${supabase.key}")
    private String SERVICE_ROLE_KEY;

    @Value("${supabase.bucket}")
    private String BUCKET;

    OkHttpClient client = new OkHttpClient();

    public String uploadImage(byte[] fileBytes, String fileExtension) {
        String fileName = UUID.randomUUID() + "." + fileExtension;

        String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + fileName;

        RequestBody body = RequestBody.create(fileBytes);

        Request request = new Request.Builder()
                .url(uploadUrl)
                .addHeader("Authorization", "Bearer " + SERVICE_ROLE_KEY)
                .addHeader("Content-Type", "application/octet-stream")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Supabase upload failed: " + response);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to Supabase", e);
        }

        return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET + "/" + fileName;
    }
}
