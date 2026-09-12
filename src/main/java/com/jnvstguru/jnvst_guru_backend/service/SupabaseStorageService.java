package com.jnvstguru.jnvst_guru_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

@Service
public class SupabaseStorageService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final String bucket;

    public SupabaseStorageService(
            @Value("${supabase.url:}") String supabaseUrl,
            @Value("${supabase.service-role-key:}") String serviceRoleKey,
            @Value("${supabase.storage-bucket:mat-images}") String bucket) {
        this.supabaseUrl = supabaseUrl == null ? "" : supabaseUrl.trim();
        this.serviceRoleKey = serviceRoleKey == null ? "" : serviceRoleKey.trim();
        this.bucket = bucket == null ? "" : bucket.trim();
    }

    public String upload(String path, InputStream content, String contentType) {
        if (supabaseUrl.isBlank() || serviceRoleKey.isBlank() || bucket.isBlank()) {
            throw new IllegalStateException("Supabase Storage is not configured");
        }
        try {
            String endpoint = supabaseUrl.replaceAll("/+$", "")
                    + "/storage/v1/object/" + bucket + "/" + path;
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .header("Content-Type", Objects.requireNonNullElse(contentType, "application/octet-stream"))
                    .header("x-upsert", "true")
                    .PUT(HttpRequest.BodyPublishers.ofInputStream(() -> content))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Supabase Storage upload failed: HTTP " + response.statusCode());
            }
            return path;
        } catch (IOException ex) {
            throw new IllegalStateException("Supabase Storage upload failed", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Supabase Storage upload was interrupted", ex);
        }
    }
}
