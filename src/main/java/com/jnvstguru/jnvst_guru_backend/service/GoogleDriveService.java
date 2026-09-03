package com.jnvstguru.jnvst_guru_backend.service;

import java.io.InputStream;

public interface GoogleDriveService {
    InputStream downloadFile(String fileId);
}
