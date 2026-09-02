package com.jnvstguru.jnvst_guru_backend.api.dto;

import com.jnvstguru.jnvst_guru_backend.domain.ArithmeticQuestionEnums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ArithmeticQuestionRequest(
        @NotBlank(message = "Question text is required")
        String questionText,

        @NotNull(message = "Question type is required")
        ArithmeticQuestionEnums.QuestionType questionType,

        @NotBlank(message = "Option A is required")
        String optionA,

        @NotBlank(message = "Option B is required")
        String optionB,

        @NotBlank(message = "Option C is required")
        String optionC,

        @NotBlank(message = "Option D is required")
        String optionD,

        @NotNull(message = "Correct option is required")
        @Pattern(regexp = "[A-D]", message = "Correct option must be A, B, C, or D")
        String correctOption,

        @NotNull(message = "Difficulty is required")
        ArithmeticQuestionEnums.Difficulty difficulty,

        String explanation,

        @NotNull(message = "Status is required")
        ArithmeticQuestionEnums.Status status
) {
}
