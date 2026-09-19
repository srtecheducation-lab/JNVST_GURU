package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.api.dto.StudentProgressResponse;
import com.jnvstguru.jnvst_guru_backend.domain.*;
import com.jnvstguru.jnvst_guru_backend.repository.PracticeAttemptRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class StudentProgressService {
    private static final int DEFAULT_RECENT_LIMIT = 20;

    private final ApplicationUserService applicationUserService;
    private final PracticeAttemptRepository attemptRepository;

    public StudentProgressService(ApplicationUserService applicationUserService,
                                  PracticeAttemptRepository attemptRepository) {
        this.applicationUserService = applicationUserService;
        this.attemptRepository = attemptRepository;
    }

    @Transactional(readOnly = true)
    public StudentProgressResponse getProgress(UUID authUserId, int recentPage, int recentLimit) {
        UserEntity user = applicationUserService.findByAuthUserId(authUserId);
        long userId = user.getId();
        PracticeAttemptRepository.ProgressAggregateProjection overall =
                attemptRepository.aggregateLatestProgress(userId);

        List<StudentProgressResponse.SubjectProgress> subjects = new ArrayList<>();
        Map<PracticeSubject, PracticeAttemptRepository.SubjectProgressProjection> subjectRows =
                new EnumMap<>(PracticeSubject.class);
        for (PracticeAttemptRepository.SubjectProgressProjection row :
                attemptRepository.aggregateLatestProgressBySubject(userId)) {
            subjectRows.put(PracticeSubject.valueOf(row.getSubject()), row);
        }
        for (PracticeSubject subject : PracticeSubject.values()) {
            PracticeAttemptRepository.SubjectProgressProjection row = subjectRows.get(subject);
            subjects.add(row == null
                    ? new StudentProgressResponse.SubjectProgress(subject, 0, 0, 0, 0, 0, 0, accuracy(0, 0))
                    : subjectProgress(subject, row));
        }

        List<StudentProgressResponse.TopicProgress> topics =
                attemptRepository.aggregateLatestProgressByTopic(userId).stream()
                        .map(this::topicProgress)
                        .toList();

        int page = Math.max(recentPage, 0);
        int limit = recentLimit > 0 ? recentLimit : DEFAULT_RECENT_LIMIT;
        List<StudentProgressResponse.RecentAttempt> recentAttempts =
                attemptRepository.findByUser_IdOrderBySubmittedAtDescIdDesc(
                                userId, PageRequest.of(page, limit))
                        .getContent().stream()
                        .map(this::recentAttempt)
                        .toList();

        return new StudentProgressResponse(summary(overall), subjects, topics, recentAttempts);
    }

    private StudentProgressResponse.SubjectProgress subjectProgress(
            PracticeSubject subject, PracticeAttemptRepository.SubjectProgressProjection row) {
        return new StudentProgressResponse.SubjectProgress(subject, row.getAttempts(), row.getQuestions(),
                row.getCorrect(), row.getWrong(), row.getUnanswered(), row.getScore(),
                accuracy(row.getCorrect(), row.getQuestions()));
    }

    private StudentProgressResponse.TopicProgress topicProgress(
            PracticeAttemptRepository.TopicProgressProjection row) {
        return new StudentProgressResponse.TopicProgress(
                PracticeSubject.valueOf(row.getSubject()),
                row.getTopic() == null ? null : ArithmeticQuestionEnums.QuestionType.valueOf(row.getTopic()),
                row.getTopicId(), row.getAttempts(), row.getQuestions(), row.getCorrect(),
                row.getWrong(), row.getUnanswered(), row.getScore(),
                accuracy(row.getCorrect(), row.getQuestions()));
    }

    private StudentProgressResponse.ProgressSummary summary(
            PracticeAttemptRepository.ProgressAggregateProjection row) {
        return new StudentProgressResponse.ProgressSummary(row.getAttempts(), row.getQuestions(),
                row.getCorrect(), row.getWrong(), row.getUnanswered(), row.getScore(),
                accuracy(row.getCorrect(), row.getQuestions()));
    }

    private StudentProgressResponse.RecentAttempt recentAttempt(PracticeAttemptEntity attempt) {
        LanguageCode language = attempt.getLanguageCode() == null
                ? null : LanguageCode.fromCode(attempt.getLanguageCode());
        return new StudentProgressResponse.RecentAttempt(attempt.getId(), attempt.getPracticeMode(),
                attempt.getSubject(), attempt.getTopic(), attempt.getTopicId(), attempt.getDifficulty(),
                language, attempt.getPageNumber(), attempt.getScore(), attempt.getQuestionCount(),
                attempt.getCorrectCount(), attempt.getWrongCount(), attempt.getUnansweredCount(),
                attempt.getSubmittedAt());
    }

    private BigDecimal accuracy(long correct, long questions) {
        return questions == 0
                ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(correct)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(questions), 2, RoundingMode.HALF_UP);
    }
}
