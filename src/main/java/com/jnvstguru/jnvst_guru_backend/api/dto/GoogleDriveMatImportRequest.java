package com.jnvstguru.jnvst_guru_backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleDriveMatImportRequest(@NotBlank String folderId) {}
