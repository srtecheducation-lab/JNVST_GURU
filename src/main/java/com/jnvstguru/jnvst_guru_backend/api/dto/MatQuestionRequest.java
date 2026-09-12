package com.jnvstguru.jnvst_guru_backend.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record MatQuestionRequest(
        @PositiveOrZero Long topicId,
        @NotBlank String questionImageUrl,
        @NotBlank String optionAImageUrl,
        @NotBlank String optionBImageUrl,
        @NotBlank String optionCImageUrl,
        @NotBlank String optionDImageUrl,
        @NotBlank @Pattern(regexp = "[A-D]") String correctOption,
        String difficulty,
        Boolean active,
        @PositiveOrZero Integer sortOrder
) {}
