package com.jnvstguru.jnvst_guru_backend.api.dto;

import com.jnvstguru.jnvst_guru_backend.domain.Difficulty;

import java.time.OffsetDateTime;

public record MatQuestionResponse(
        Long questionId,
        String questionText,
        String correctOption,
        Difficulty difficulty,
        String explanation,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
