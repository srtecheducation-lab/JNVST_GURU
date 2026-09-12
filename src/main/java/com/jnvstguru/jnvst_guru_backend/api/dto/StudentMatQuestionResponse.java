package com.jnvstguru.jnvst_guru_backend.api.dto;

public record StudentMatQuestionResponse(
        Long id, Long topicId, String questionImageUrl, String optionAImageUrl,
        String optionBImageUrl, String optionCImageUrl, String optionDImageUrl,
        String difficulty
) {}
