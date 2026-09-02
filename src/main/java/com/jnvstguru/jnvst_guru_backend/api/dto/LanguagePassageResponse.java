package com.jnvstguru.jnvst_guru_backend.api.dto;

import java.time.OffsetDateTime;

public record LanguagePassageResponse(
        Long id,
        String passageText,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
