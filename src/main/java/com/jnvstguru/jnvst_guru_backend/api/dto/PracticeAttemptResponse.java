package com.jnvstguru.jnvst_guru_backend.api.dto;

import com.jnvstguru.jnvst_guru_backend.domain.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;

public record PracticeAttemptResponse(
        Long attemptId, PracticeMode practiceMode, PracticeSubject subject,
        ArithmeticQuestionEnums.QuestionType topic, ArithmeticQuestionEnums.Difficulty difficulty,
        Integer page, Integer score, Integer questionCount, Integer correctCount,
        Integer wrongCount, Integer unansweredCount, OffsetDateTime submittedAt,
        List<AnswerResponse> answers, @JsonInclude(JsonInclude.Include.NON_NULL) Long topicId) {
    public PracticeAttemptResponse(Long attemptId, PracticeMode practiceMode, PracticeSubject subject,
                                   ArithmeticQuestionEnums.QuestionType topic,
                                   ArithmeticQuestionEnums.Difficulty difficulty, Integer page, Integer score,
                                   Integer questionCount, Integer correctCount, Integer wrongCount,
                                   Integer unansweredCount, OffsetDateTime submittedAt,
                                   List<AnswerResponse> answers) {
        this(attemptId, practiceMode, subject, topic, difficulty, page, score, questionCount, correctCount,
                wrongCount, unansweredCount, submittedAt, answers, null);
    }
    public record AnswerResponse(Long questionId, String selectedOption, String correctOption,
                                 boolean isCorrect,
                                 @JsonInclude(JsonInclude.Include.NON_NULL) Long matQuestionId,
                                 @JsonInclude(JsonInclude.Include.NON_NULL) String explanation) {}
}
