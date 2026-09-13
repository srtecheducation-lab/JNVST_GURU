package com.jnvstguru.jnvst_guru_backend.api.dto;

import com.jnvstguru.jnvst_guru_backend.domain.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public record PracticeStatusResponse(
        PracticeMode practiceMode, PracticeSubject subject,
        ArithmeticQuestionEnums.QuestionType topic,
        ArithmeticQuestionEnums.Difficulty difficulty, List<SetStatus> sets,
        @JsonInclude(JsonInclude.Include.NON_NULL) Long topicId) {
    public record SetStatus(Integer page, Integer setNumber, Integer questionCount, boolean completed) {}
}
