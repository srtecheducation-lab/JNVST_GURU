package com.jnvstguru.jnvst_guru_backend.api.dto;

import com.jnvstguru.jnvst_guru_backend.domain.Difficulty;

import java.time.OffsetDateTime;

public record LanguageQuestionResponse(
        Long questionId,
        Long passageId,
        String questionText,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String correctOption,
        Difficulty difficulty,
        String explanation,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
