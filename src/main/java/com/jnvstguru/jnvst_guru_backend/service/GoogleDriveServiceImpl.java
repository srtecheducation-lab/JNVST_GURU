package com.jnvstguru.jnvst_guru_backend.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@ConditionalOnBean(Drive.class)
public class GoogleDriveServiceImpl implements GoogleDriveService {
    private final Drive drive;

    public GoogleDriveServiceImpl(Drive drive) {
        this.drive = drive;
    }

    @Override
    public InputStream downloadFile(String fileId) {
        if (fileId == null || fileId.isBlank()) {
            throw new GoogleDriveException("Google Drive fileId must not be blank");
        }

        try {
            return drive.files().get(fileId.trim())
                    .setFields("id")
                    .executeMedia()
                    .getContent();
        } catch (GoogleJsonResponseException ex) {
            int statusCode = ex.getStatusCode();
            if (statusCode == 404) {
                throw new GoogleDriveException("Google Drive file was not found", ex);
            }
            if (statusCode == 401 || statusCode == 403) {
                throw new GoogleDriveException("Google Drive access was denied", ex);
            }
            throw new GoogleDriveException("Google Drive request failed", ex);
        } catch (IOException ex) {
            throw new GoogleDriveException("Google Drive request failed", ex);
        }
    }
}
