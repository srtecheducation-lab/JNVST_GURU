package com.jnvstguru.jnvst_guru_backend.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<DriveFile> listChildren(String folderId) {
        if (folderId == null || folderId.isBlank()) {
            throw new GoogleDriveException("Google Drive folderId must not be blank");
        }
        try {
            List<DriveFile> result = new ArrayList<>();
            String pageToken = null;
            do {
                var page = drive.files().list()
                        .setQ("'" + folderId.trim() + "' in parents and trashed = false")
                        .setFields("nextPageToken,files(id,name,mimeType)")
                        .setPageToken(pageToken)
                        .execute();
                if (page.getFiles() != null) {
                    page.getFiles().forEach(file ->
                            result.add(new DriveFile(file.getId(), file.getName(), file.getMimeType())));
                }
                pageToken = page.getNextPageToken();
            } while (pageToken != null && !pageToken.isBlank());
            return result;
        } catch (GoogleJsonResponseException ex) {
            int statusCode = ex.getStatusCode();
            if (statusCode == 404) throw new GoogleDriveException("Google Drive folder was not found", ex);
            if (statusCode == 401 || statusCode == 403) throw new GoogleDriveException("Google Drive access was denied", ex);
            throw new GoogleDriveException("Google Drive request failed", ex);
        } catch (IOException ex) {
            throw new GoogleDriveException("Google Drive request failed", ex);
        }
    }
}
