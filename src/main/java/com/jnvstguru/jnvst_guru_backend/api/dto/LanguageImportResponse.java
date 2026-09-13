package com.jnvstguru.jnvst_guru_backend.api.dto;

import java.util.List;

public record LanguageImportResponse(
        String batchNo,
        int processedFiles,
        int importedQuestions,
        int skippedQuestions,
        int failedFiles,
        List<String> missingFiles,
        List<String> errors) {}
