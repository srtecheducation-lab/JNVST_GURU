package com.jnvstguru.jnvst_guru_backend.api.dto;

import com.jnvstguru.jnvst_guru_backend.domain.ArithmeticQuestionEnums;

public record StudentArithmeticQuestionResponse(
        Long id,
        ArithmeticQuestionEnums.QuestionType questionType,
        String questionText,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        ArithmeticQuestionEnums.Difficulty difficulty
) {
}
