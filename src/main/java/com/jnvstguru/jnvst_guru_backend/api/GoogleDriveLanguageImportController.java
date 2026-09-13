package com.jnvstguru.jnvst_guru_backend.api;

import com.jnvstguru.jnvst_guru_backend.api.dto.GoogleDriveLanguageImportRequest;
import com.jnvstguru.jnvst_guru_backend.api.dto.LanguageImportResponse;
import com.jnvstguru.jnvst_guru_backend.service.GoogleDriveLanguageImportService;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/import/google-drive")
@ConditionalOnProperty(prefix = "google.drive", name = "enabled", havingValue = "true")
public class GoogleDriveLanguageImportController {
    private final GoogleDriveLanguageImportService service;

    public GoogleDriveLanguageImportController(GoogleDriveLanguageImportService service) {
        this.service = service;
    }

    @PostMapping("/language")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LanguageImportResponse> importLanguage(
            @Valid @RequestBody GoogleDriveLanguageImportRequest request) {
        return ResponseEntity.ok(service.importFolder(request));
    }
}
