package com.jnvstguru.jnvst_guru_backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleDriveLanguageImportRequest(@NotBlank String folderId) {}
