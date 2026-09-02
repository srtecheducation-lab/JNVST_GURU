package com.jnvstguru.jnvst_guru_backend.api.dto;

import com.jnvstguru.jnvst_guru_backend.domain.ArithmeticQuestionEnums;

import java.time.OffsetDateTime;

public record ArithmeticQuestionResponse(
        Long id,
        String questionText,
        ArithmeticQuestionEnums.QuestionType questionType,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String correctOption,
        ArithmeticQuestionEnums.Difficulty difficulty,
        String explanation,
        ArithmeticQuestionEnums.Status status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
