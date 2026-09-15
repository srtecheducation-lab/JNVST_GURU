package com.jnvstguru.jnvst_guru_backend.api.dto;

import com.jnvstguru.jnvst_guru_backend.domain.*;
import java.util.List;

public record PracticeAttemptRequest(
        PracticeMode practiceMode,
        PracticeSubject subject,
        ArithmeticQuestionEnums.QuestionType topic,
        ArithmeticQuestionEnums.Difficulty difficulty,
        Integer page,
        List<AnswerRequest> answers,
        Long topicId,
        LanguageCode language) {
    public PracticeAttemptRequest(PracticeMode practiceMode, PracticeSubject subject,
                                  ArithmeticQuestionEnums.QuestionType topic,
                                  ArithmeticQuestionEnums.Difficulty difficulty, Integer page,
                                  List<AnswerRequest> answers) {
        this(practiceMode, subject, topic, difficulty, page, answers, null, null);
    }
    public PracticeAttemptRequest(PracticeMode practiceMode, PracticeSubject subject,
                                  ArithmeticQuestionEnums.QuestionType topic,
                                  ArithmeticQuestionEnums.Difficulty difficulty, Integer page,
                                  List<AnswerRequest> answers, Long topicId) {
        this(practiceMode, subject, topic, difficulty, page, answers, topicId, null);
    }
    public record AnswerRequest(Long questionId, String selectedOption) {}
}
