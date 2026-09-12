package com.jnvstguru.jnvst_guru_backend.service;

import java.io.InputStream;
import java.util.List;

public interface GoogleDriveService {
    InputStream downloadFile(String fileId);
    List<DriveFile> listChildren(String folderId);

    record DriveFile(String id, String name, String mimeType) {}
}
