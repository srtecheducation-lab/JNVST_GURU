package com.jnvstguru.jnvst_guru_backend.api.dto;

import java.util.List;

public record StudentLanguagePassageResponse(
        Long passageId,
        Integer passageNumber,
        String passageText,
        List<Question> questions) {

    public record Question(
            Long questionId,
            Integer questionNumber,
            String questionText,
            String optionA,
            String optionB,
            String optionC,
            String optionD) {
    }
}
