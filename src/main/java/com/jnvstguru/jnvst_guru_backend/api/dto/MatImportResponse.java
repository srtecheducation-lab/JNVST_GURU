package com.jnvstguru.jnvst_guru_backend.api.dto;

import java.util.List;

public record MatImportResponse(int imported, int skipped, List<MatImportFailure> failures) {
    public record MatImportFailure(String questionNo, String message) {}
}
