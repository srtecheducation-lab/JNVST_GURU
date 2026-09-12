package com.jnvstguru.jnvst_guru_backend.api;

import com.jnvstguru.jnvst_guru_backend.api.dto.GoogleDriveMatImportRequest;
import com.jnvstguru.jnvst_guru_backend.api.dto.MatImportResponse;
import com.jnvstguru.jnvst_guru_backend.service.GoogleDriveMatImportService;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/import/google-drive")
@ConditionalOnProperty(prefix = "google.drive", name = "enabled", havingValue = "true")
public class GoogleDriveMatImportController {
    private final GoogleDriveMatImportService service;

    public GoogleDriveMatImportController(GoogleDriveMatImportService service) {
        this.service = service;
    }

    @PostMapping("/mat")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MatImportResponse> importMat(@Valid @RequestBody GoogleDriveMatImportRequest request) {
        return ResponseEntity.ok(service.importFolder(request));
    }
}
