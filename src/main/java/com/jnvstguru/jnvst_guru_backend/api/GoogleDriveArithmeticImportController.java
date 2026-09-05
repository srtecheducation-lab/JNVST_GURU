package com.jnvstguru.jnvst_guru_backend.api;

import com.jnvstguru.jnvst_guru_backend.api.dto.ArithmeticImportResponse;
import com.jnvstguru.jnvst_guru_backend.api.dto.GoogleDriveArithmeticImportRequest;
import com.jnvstguru.jnvst_guru_backend.service.GoogleDriveArithmeticImportService;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/import/google-drive")
@ConditionalOnProperty(prefix = "google.drive", name = "enabled", havingValue = "true")
public class GoogleDriveArithmeticImportController {
    private final GoogleDriveArithmeticImportService importService;

    public GoogleDriveArithmeticImportController(GoogleDriveArithmeticImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/arithmetic")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArithmeticImportResponse> importArithmetic(
            @Valid @RequestBody GoogleDriveArithmeticImportRequest request) {
        return ResponseEntity.ok(importService.importFile(request));
    }

    @PostMapping("/arithmetic/bengali")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArithmeticImportResponse> importBengaliArithmetic(
            @Valid @RequestBody GoogleDriveArithmeticImportRequest request) {
        return ResponseEntity.ok(importService.importBengaliFile(request));
    }
}
