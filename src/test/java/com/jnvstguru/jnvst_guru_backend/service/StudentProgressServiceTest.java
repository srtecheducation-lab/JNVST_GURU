package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.api.dto.StudentProgressResponse;
import com.jnvstguru.jnvst_guru_backend.domain.*;
import com.jnvstguru.jnvst_guru_backend.repository.PracticeAttemptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentProgressServiceTest {
    private static final UUID AUTH_ID = UUID.randomUUID();

    @Mock ApplicationUserService userService;
    @Mock PracticeAttemptRepository repository;

    @Test
    void noAttemptsReturnsAllSubjectsWithZeroAccuracy() {
        UserEntity user = user(7L);
        when(userService.findByAuthUserId(AUTH_ID)).thenReturn(user);
        when(repository.aggregateLatestProgress(7L)).thenReturn(aggregate(0, 0, 0, 0, 0, 0));
        when(repository.aggregateLatestProgressBySubject(7L)).thenReturn(List.of());
        when(repository.aggregateLatestProgressByTopic(7L)).thenReturn(List.of());
        when(repository.findByUser_IdOrderBySubmittedAtDescIdDesc(eq(7L), any()))
                .thenReturn(new PageImpl<>(List.of()));

        StudentProgressResponse result = service().getProgress(AUTH_ID, 0, 20);

        assertEquals(0, result.overall().attempts());
        assertEquals(3, result.subjects().size());
        assertTrue(result.subjects().stream().allMatch(s -> s.accuracy().toPlainString().equals("0.00")));
        assertTrue(result.topics().isEmpty());
        assertTrue(result.recentAttempts().isEmpty());
    }

    @Test
    void mixedSubjectAndTopicRowsAreMappedAndAccuracyIsCalculated() {
        UserEntity user = user(8L);
        when(userService.findByAuthUserId(AUTH_ID)).thenReturn(user);
        when(repository.aggregateLatestProgress(8L)).thenReturn(aggregate(4, 80, 68, 8, 4, 68));
        when(repository.aggregateLatestProgressBySubject(8L)).thenReturn(List.of(
                subject("ARITHMETIC", 2, 40, 34, 4, 2, 34),
                subject("MAT", 1, 20, 17, 2, 1, 17),
                subject("LANGUAGE", 1, 20, 17, 2, 1, 17)));
        when(repository.aggregateLatestProgressByTopic(8L)).thenReturn(List.of(
                topic("ARITHMETIC", "FRACTION", null, 1, 20, 17, 2, 1, 17),
                topic("MAT", null, 12L, 1, 20, 17, 2, 1, 17)));
        when(repository.findByUser_IdOrderBySubmittedAtDescIdDesc(eq(8L), any()))
                .thenReturn(new PageImpl<>(List.of()));

        StudentProgressResponse result = service().getProgress(AUTH_ID, 0, 20);

        assertEquals("85.00", result.overall().accuracy().toPlainString());
        assertEquals(3, result.subjects().size());
        assertEquals(2, result.topics().size());
        assertEquals(ArithmeticQuestionEnums.QuestionType.FRACTION, result.topics().get(0).topic());
        assertEquals(12L, result.topics().get(1).topicId());
        assertEquals(0, result.topics().stream().filter(t -> t.subject() == PracticeSubject.LANGUAGE).count());
    }

    @Test
    void recentAttemptsKeepHistoryAndLanguageMapping() {
        UserEntity user = user(9L);
        PracticeAttemptEntity first = attempt(101L, 10, null);
        PracticeAttemptEntity second = attempt(105L, 17, "en");
        when(userService.findByAuthUserId(AUTH_ID)).thenReturn(user);
        when(repository.aggregateLatestProgress(9L)).thenReturn(aggregate(1, 20, 17, 2, 1, 17));
        when(repository.aggregateLatestProgressBySubject(9L)).thenReturn(List.of(
                subject("ARITHMETIC", 1, 20, 17, 2, 1, 17)));
        when(repository.aggregateLatestProgressByTopic(9L)).thenReturn(List.of());
        when(repository.findByUser_IdOrderBySubmittedAtDescIdDesc(eq(9L), any()))
                .thenReturn(new PageImpl<>(List.of(second, first)));

        StudentProgressResponse result = service().getProgress(AUTH_ID, 0, 20);

        assertEquals(2, result.recentAttempts().size());
        assertEquals(17, result.recentAttempts().get(0).score());
        assertEquals(LanguageCode.ENGLISH, result.recentAttempts().get(0).language());
        verify(repository).aggregateLatestProgress(9L);
    }

    @Test
    void paginationIsPassedToRecentAttemptQuery() {
        UserEntity user = user(10L);
        when(userService.findByAuthUserId(AUTH_ID)).thenReturn(user);
        when(repository.aggregateLatestProgress(10L)).thenReturn(aggregate(0, 0, 0, 0, 0, 0));
        when(repository.aggregateLatestProgressBySubject(10L)).thenReturn(List.of());
        when(repository.aggregateLatestProgressByTopic(10L)).thenReturn(List.of());
        when(repository.findByUser_IdOrderBySubmittedAtDescIdDesc(eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of()));

        service().getProgress(AUTH_ID, 2, 5);

        verify(repository).findByUser_IdOrderBySubmittedAtDescIdDesc(eq(10L),
                argThat(page -> page.getPageNumber() == 2 && page.getPageSize() == 5));
    }

    private StudentProgressService service() {
        return new StudentProgressService(userService, repository);
    }

    private static UserEntity user(long id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        return user;
    }

    private static PracticeAttemptEntity attempt(long id, int score, String language) {
        PracticeAttemptEntity attempt = new PracticeAttemptEntity();
        attempt.setPracticeMode(PracticeMode.TOPIC);
        attempt.setSubject(language == null ? PracticeSubject.ARITHMETIC : PracticeSubject.LANGUAGE);
        attempt.setTopic(language == null ? ArithmeticQuestionEnums.QuestionType.FRACTION : null);
        attempt.setDifficulty(language == null ? ArithmeticQuestionEnums.Difficulty.EASY : null);
        attempt.setPageNumber(0);
        attempt.setQuestionCount(20);
        attempt.setScore(score);
        attempt.setCorrectCount(score);
        attempt.setWrongCount(20 - score - 1);
        attempt.setUnansweredCount(1);
        attempt.setLanguageCode(language);
        return attempt;
    }

    private static PracticeAttemptRepository.ProgressAggregateProjection aggregate(
            long attempts, long questions, long correct, long wrong, long unanswered, long score) {
        return new Aggregate(attempts, questions, correct, wrong, unanswered, score);
    }

    private static PracticeAttemptRepository.SubjectProgressProjection subject(
            String subject, long attempts, long questions, long correct, long wrong, long unanswered, long score) {
        return new Subject(subject, attempts, questions, correct, wrong, unanswered, score);
    }

    private static PracticeAttemptRepository.TopicProgressProjection topic(
            String subject, String topic, Long topicId, long attempts, long questions,
            long correct, long wrong, long unanswered, long score) {
        return new Topic(subject, topic, topicId, attempts, questions, correct, wrong, unanswered, score);
    }

    private record Aggregate(long attempts, long questions, long correct, long wrong,
                             long unanswered, long score)
            implements PracticeAttemptRepository.ProgressAggregateProjection {
        public Long getAttempts() { return attempts; }
        public Long getQuestions() { return questions; }
        public Long getCorrect() { return correct; }
        public Long getWrong() { return wrong; }
        public Long getUnanswered() { return unanswered; }
        public Long getScore() { return score; }
    }

    private record Subject(String subject, long attempts, long questions, long correct, long wrong,
                           long unanswered, long score)
            implements PracticeAttemptRepository.SubjectProgressProjection {
        public String getSubject() { return subject; }
        public Long getAttempts() { return attempts; }
        public Long getQuestions() { return questions; }
        public Long getCorrect() { return correct; }
        public Long getWrong() { return wrong; }
        public Long getUnanswered() { return unanswered; }
        public Long getScore() { return score; }
    }

    private record Topic(String subject, String topic, Long topicId, long attempts, long questions,
                         long correct, long wrong, long unanswered, long score)
            implements PracticeAttemptRepository.TopicProgressProjection {
        public String getSubject() { return subject; }
        public String getTopic() { return topic; }
        public Long getTopicId() { return topicId; }
        public Long getAttempts() { return attempts; }
        public Long getQuestions() { return questions; }
        public Long getCorrect() { return correct; }
        public Long getWrong() { return wrong; }
        public Long getUnanswered() { return unanswered; }
        public Long getScore() { return score; }
    }
}
