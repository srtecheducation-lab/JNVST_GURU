package com.jnvstguru.jnvst_guru_backend.api.dto;

import com.jnvstguru.jnvst_guru_backend.domain.PaperQuestionType;

import java.time.OffsetDateTime;

public record PaperQuestionResponse(
        Long id,
        Long paperId,
        Long questionId,
        Integer batchNo,
        Integer questionNumber,
        PaperQuestionType questionType,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
