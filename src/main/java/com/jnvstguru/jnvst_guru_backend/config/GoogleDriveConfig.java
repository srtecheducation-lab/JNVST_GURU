package com.jnvstguru.jnvst_guru_backend.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.jnvstguru.jnvst_guru_backend.service.GoogleDriveException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

@Configuration
@EnableConfigurationProperties(GoogleDriveProperties.class)
@ConditionalOnProperty(prefix = "google.drive", name = "enabled", havingValue = "true")
public class GoogleDriveConfig {
    @Bean
    public Drive googleDrive(GoogleDriveProperties properties) {
        String credentialsJson = properties.getCredentialsJson();
        if (credentialsJson == null || credentialsJson.isBlank()) {
            throw new GoogleDriveException(
                    "Google Drive is enabled but GOOGLE_DRIVE_SERVICE_ACCOUNT_JSON is not configured");
        }

        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)))
                    .createScoped(DriveScopes.DRIVE_READONLY);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
            return new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    requestInitializer)
                    .setApplicationName("jnvst-guru-backend")
                    .build();
        } catch (IOException | GeneralSecurityException ex) {
            throw new GoogleDriveException("Google Drive service-account configuration is invalid", ex);
        }
    }
}
