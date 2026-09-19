package com.jnvstguru.jnvst_guru_backend.api.dto;

import com.jnvstguru.jnvst_guru_backend.domain.ArithmeticQuestionEnums;
import com.jnvstguru.jnvst_guru_backend.domain.LanguageCode;
import com.jnvstguru.jnvst_guru_backend.domain.PracticeMode;
import com.jnvstguru.jnvst_guru_backend.domain.PracticeSubject;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record StudentProgressResponse(
        ProgressSummary overall,
        List<SubjectProgress> subjects,
        List<TopicProgress> topics,
        List<RecentAttempt> recentAttempts) {

    public record ProgressSummary(
            long attempts, long questions, long correct, long wrong,
            long unanswered, long score, BigDecimal accuracy) {}

    public record SubjectProgress(
            PracticeSubject subject, long attempts, long questions, long correct,
            long wrong, long unanswered, long score, BigDecimal accuracy) {}

    public record TopicProgress(
            PracticeSubject subject,
            ArithmeticQuestionEnums.QuestionType topic,
            Long topicId,
            long attempts,
            long questions,
            long correct,
            long wrong,
            long unanswered,
            long score,
            BigDecimal accuracy) {}

    public record RecentAttempt(
            Long attemptId,
            PracticeMode practiceMode,
            PracticeSubject subject,
            ArithmeticQuestionEnums.QuestionType topic,
            Long topicId,
            ArithmeticQuestionEnums.Difficulty difficulty,
            LanguageCode language,
            Integer page,
            Integer score,
            Integer questionCount,
            Integer correctCount,
            Integer wrongCount,
            Integer unansweredCount,
            OffsetDateTime submittedAt) {}
}
