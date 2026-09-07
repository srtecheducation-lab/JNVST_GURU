package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.api.dto.*;
import com.jnvstguru.jnvst_guru_backend.domain.*;
import com.jnvstguru.jnvst_guru_backend.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.*;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class PracticeAttemptServiceTest {
    private static final UUID AUTH_ID = UUID.randomUUID();

    @Mock ApplicationUserService userService;
    @Mock ArithmeticQuestionRepository questionRepository;
    @Mock PracticeAttemptRepository attemptRepository;
    @Mock PracticeAttemptAnswerRepository answerRepository;
    @InjectMocks PracticeAttemptService service;

    private UserEntity user;
    private ArithmeticQuestionRepository.PracticeQuestionProjection first;
    private ArithmeticQuestionRepository.PracticeQuestionProjection second;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        first = question(7L, "A");
        second = question(36L, "C");
        when(userService.findByAuthUserId(AUTH_ID)).thenReturn(user);
        when(questionRepository.findActivePracticeQuestions(any(), any(), any())).thenReturn(List.of(first, second));
        when(attemptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void subjectSubmissionStoresCorrectWrongAndUnanswered() {
        PracticeAttemptResponse result = service.submit(AUTH_ID, new PracticeAttemptRequest(
                PracticeMode.SUBJECT, PracticeSubject.ARITHMETIC, null,
                ArithmeticQuestionEnums.Difficulty.EASY, 0,
                List.of(new PracticeAttemptRequest.AnswerRequest(7L, "A"),
                        new PracticeAttemptRequest.AnswerRequest(36L, "B"))));

        assertEquals(1, result.correctCount());
        assertEquals(1, result.wrongCount());
        assertEquals(0, result.unansweredCount());
        verify(answerRepository).saveAll(argThat(iterable -> {
            List<PracticeAttemptAnswerEntity> list = StreamSupport.stream(iterable.spliterator(), false).toList();
            return list.size() == 2
                    && list.get(0).getCorrectOption().equals("A")
                    && list.get(1).getCorrectOption().equals("C");
        }));
    }

    @Test
    void topicSubmissionCountsUnansweredAndRequiresTopic() {
        PracticeAttemptResponse result = service.submit(AUTH_ID, new PracticeAttemptRequest(
                PracticeMode.TOPIC, PracticeSubject.ARITHMETIC, ArithmeticQuestionEnums.QuestionType.FRACTION,
                ArithmeticQuestionEnums.Difficulty.EASY, 0, List.of()));
        assertEquals(0, result.correctCount());
        assertEquals(0, result.wrongCount());
        assertEquals(2, result.unansweredCount());

        assertThrows(IllegalArgumentException.class, () -> service.submit(AUTH_ID, new PracticeAttemptRequest(
                PracticeMode.TOPIC, PracticeSubject.ARITHMETIC, null,
                ArithmeticQuestionEnums.Difficulty.EASY, 0, List.of())));
    }

    @Test
    void subjectRejectsTopicAndUnknownQuestionIds() {
        assertThrows(IllegalArgumentException.class, () -> service.submit(AUTH_ID, new PracticeAttemptRequest(
                PracticeMode.SUBJECT, PracticeSubject.ARITHMETIC, ArithmeticQuestionEnums.QuestionType.FRACTION,
                ArithmeticQuestionEnums.Difficulty.EASY, 0, List.of())));
        assertThrows(IllegalArgumentException.class, () -> service.submit(AUTH_ID, new PracticeAttemptRequest(
                PracticeMode.SUBJECT, PracticeSubject.ARITHMETIC, null,
                ArithmeticQuestionEnums.Difficulty.EASY, 0,
                List.of(new PracticeAttemptRequest.AnswerRequest(999L, "A")))));
    }

    @Test
    void resubmissionCreatesAnotherAttempt() {
        PracticeAttemptRequest request = new PracticeAttemptRequest(
                PracticeMode.SUBJECT, PracticeSubject.ARITHMETIC, null,
                ArithmeticQuestionEnums.Difficulty.EASY, 0, List.of());
        service.submit(AUTH_ID, request);
        service.submit(AUTH_ID, request);
        verify(attemptRepository, times(2)).save(any(PracticeAttemptEntity.class));
    }

    @Test
    void statusUsesExactSetAndLatestUsesLatestRepositoryQuery() {
        when(questionRepository.countActivePracticeQuestions(ArithmeticQuestionEnums.QuestionType.FRACTION,
                ArithmeticQuestionEnums.Difficulty.EASY)).thenReturn(21L);
        when(attemptRepository.findCompletedPages(eq(user), eq(PracticeMode.TOPIC), eq(PracticeSubject.ARITHMETIC),
                eq(ArithmeticQuestionEnums.QuestionType.FRACTION), eq(ArithmeticQuestionEnums.Difficulty.EASY)))
                .thenReturn(List.of(0));
        PracticeStatusResponse status = service.getStatus(AUTH_ID, PracticeMode.TOPIC, PracticeSubject.ARITHMETIC,
                ArithmeticQuestionEnums.QuestionType.FRACTION, ArithmeticQuestionEnums.Difficulty.EASY);
        assertEquals(2, status.sets().size());
        assertTrue(status.sets().get(0).completed());
        assertFalse(status.sets().get(1).completed());

        PracticeAttemptEntity latest = new PracticeAttemptEntity();
        when(attemptRepository.findFirstByUserAndPracticeModeAndSubjectAndTopicAndDifficultyAndPageNumberOrderBySubmittedAtDescIdDesc(
                eq(user), eq(PracticeMode.TOPIC), eq(PracticeSubject.ARITHMETIC),
                eq(ArithmeticQuestionEnums.QuestionType.FRACTION), eq(ArithmeticQuestionEnums.Difficulty.EASY), eq(0)))
                .thenReturn(latest);
        when(answerRepository.findByAttemptOrderByQuestionId(latest)).thenReturn(List.of());
        assertNotNull(service.getLatest(AUTH_ID, PracticeMode.TOPIC, PracticeSubject.ARITHMETIC,
                ArithmeticQuestionEnums.QuestionType.FRACTION, ArithmeticQuestionEnums.Difficulty.EASY, 0));
    }

    private ArithmeticQuestionRepository.PracticeQuestionProjection question(Long id, String correct) {
        return new ArithmeticQuestionRepository.PracticeQuestionProjection() {
            public Long getQuestionId() { return id; }
            public String getCorrectOption() { return correct; }
        };
    }
}
