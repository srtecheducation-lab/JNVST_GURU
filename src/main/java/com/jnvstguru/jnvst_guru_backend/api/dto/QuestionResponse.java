package com.jnvstguru.jnvst_guru_backend.api.dto;

import com.jnvstguru.jnvst_guru_backend.domain.QuestionStatus;

import java.time.OffsetDateTime;

public record QuestionResponse(
        Long id,
        String contentHash,
        QuestionStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
