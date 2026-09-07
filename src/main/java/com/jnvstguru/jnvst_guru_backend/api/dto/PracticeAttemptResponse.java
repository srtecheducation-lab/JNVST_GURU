package com.jnvstguru.jnvst_guru_backend.api.dto;

import com.jnvstguru.jnvst_guru_backend.domain.*;
import java.time.OffsetDateTime;
import java.util.List;

public record PracticeAttemptResponse(
        Long attemptId, PracticeMode practiceMode, PracticeSubject subject,
        ArithmeticQuestionEnums.QuestionType topic, ArithmeticQuestionEnums.Difficulty difficulty,
        Integer page, Integer score, Integer questionCount, Integer correctCount,
        Integer wrongCount, Integer unansweredCount, OffsetDateTime submittedAt,
        List<AnswerResponse> answers) {
    public record AnswerResponse(Long questionId, String selectedOption, String correctOption, boolean isCorrect) {}
}
