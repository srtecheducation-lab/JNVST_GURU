package com.jnvstguru.jnvst_guru_backend.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GoogleDriveArithmeticImportRequest(
        @NotBlank String fileId,
        @NotNull Long paperId,
        @NotBlank String batchNo
) {
}
