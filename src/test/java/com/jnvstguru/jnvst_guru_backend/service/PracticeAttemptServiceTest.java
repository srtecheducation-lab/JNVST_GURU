package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.api.dto.*;
import com.jnvstguru.jnvst_guru_backend.domain.*;
import com.jnvstguru.jnvst_guru_backend.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.*;

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
    @Mock LanguageQuestionRepository languageQuestionRepository;
    @Mock EntityManager entityManager;
    @Mock Query nativeQuery;
    @InjectMocks PracticeAttemptService service;

    private UserEntity user;
    private ArithmeticQuestionRepository.PracticeQuestionProjection first;
    private ArithmeticQuestionRepository.PracticeQuestionProjection second;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        first = question(7L, "A");
        second = question(36L, "C");
        lenient().when(userService.findByAuthUserId(AUTH_ID)).thenReturn(user);
        lenient().when(questionRepository.findActivePracticeQuestions(any(), any(), any())).thenReturn(List.of(first, second));
        lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        lenient().when(attemptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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
        verify(nativeQuery).executeUpdate();
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
    void languageSubmissionUsesIndependentQuestionsAndStoresUnansweredAnswers() {
        LanguageQuestionIndependentEntity englishFirst = languageQuestion(101L, "A");
        LanguageQuestionIndependentEntity englishSecond = languageQuestion(102L, "C");
        when(languageQuestionRepository.findByLanguageCodeAndActiveTrueOrderByBatchNoAscQuestionNumberAscIdAsc(
                eq("en"), any())).thenReturn(new org.springframework.data.domain.PageImpl<>(
                        List.of(englishFirst, englishSecond)));
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);

        PracticeAttemptResponse result = service.submit(AUTH_ID, new PracticeAttemptRequest(
                PracticeMode.SUBJECT, PracticeSubject.LANGUAGE, null, null, 0,
                List.of(new PracticeAttemptRequest.AnswerRequest(101L, "A")),
                null, LanguageCode.ENGLISH));

        assertEquals(1, result.correctCount());
        assertEquals(0, result.wrongCount());
        assertEquals(1, result.unansweredCount());
        assertEquals(List.of(101L, 102L), result.answers().stream()
                .map(PracticeAttemptResponse.AnswerResponse::questionId).toList());
        ArgumentCaptor<PracticeAttemptEntity> attemptCaptor = ArgumentCaptor.forClass(PracticeAttemptEntity.class);
        verify(attemptRepository).save(attemptCaptor.capture());
        assertNull(attemptCaptor.getValue().getDifficulty());
        verify(nativeQuery).executeUpdate();
    }

    @Test
    void languageRejectsDifficultyAndForeignQuestionIds() {
        assertThrows(IllegalArgumentException.class, () -> service.submit(AUTH_ID, new PracticeAttemptRequest(
                PracticeMode.SUBJECT, PracticeSubject.LANGUAGE, null,
                ArithmeticQuestionEnums.Difficulty.EASY, 0, List.of(), null, LanguageCode.ENGLISH)));
        LanguageQuestionIndependentEntity bengaliQuestion = languageQuestion(201L, "B");
        when(languageQuestionRepository.findByLanguageCodeAndActiveTrueOrderByBatchNoAscQuestionNumberAscIdAsc(
                eq("bn"), any())).thenReturn(new org.springframework.data.domain.PageImpl<>(
                        List.of(bengaliQuestion)));
        assertThrows(IllegalArgumentException.class, () -> service.submit(AUTH_ID, new PracticeAttemptRequest(
                PracticeMode.SUBJECT, PracticeSubject.LANGUAGE, null, null, 0,
                List.of(new PracticeAttemptRequest.AnswerRequest(999L, "A")),
                null, LanguageCode.BENGALI)));
    }

    @Test
    void languageStatusRequiresPageAndAcceptsEnglishAndBengaliWithoutDifficulty() {
        when(languageQuestionRepository.countByLanguageCodeAndActiveTrue(anyString())).thenReturn(20L);
        when(attemptRepository.findCompletedLanguagePages(eq(user), eq(PracticeMode.SUBJECT),
                eq(PracticeSubject.LANGUAGE), anyString())).thenReturn(List.of());

        PracticeStatusResponse english = service.getStatus(AUTH_ID, PracticeMode.SUBJECT,
                PracticeSubject.LANGUAGE, null, null, null, LanguageCode.ENGLISH, 0);
        PracticeStatusResponse bengali = service.getStatus(AUTH_ID, PracticeMode.SUBJECT,
                PracticeSubject.LANGUAGE, null, null, null, LanguageCode.BENGALI, 1);

        assertNull(english.difficulty());
        assertEquals(1, english.sets().size());
        assertNull(bengali.difficulty());
        assertEquals(1, bengali.sets().size());
        PracticeStatusResponse defaultPage = service.getStatus(AUTH_ID, PracticeMode.SUBJECT,
                PracticeSubject.LANGUAGE, null, null, null, LanguageCode.ENGLISH, null);
        assertEquals(1, defaultPage.sets().size());
    }

    @Test
    void arithmeticAndMatStatusStillRequireDifficulty() {
        assertThrows(IllegalArgumentException.class, () -> service.getStatus(AUTH_ID,
                PracticeMode.SUBJECT, PracticeSubject.ARITHMETIC, null, null, null,
                null, 0));
        assertThrows(IllegalArgumentException.class, () -> service.getStatus(AUTH_ID,
                PracticeMode.SUBJECT, PracticeSubject.MAT, null, null, null,
                null, 0));
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

    private LanguageQuestionIndependentEntity languageQuestion(Long id, String correct) {
        LanguageQuestionIndependentEntity question = mock(LanguageQuestionIndependentEntity.class);
        lenient().when(question.getId()).thenReturn(id);
        lenient().when(question.getCorrectOption()).thenReturn(correct);
        return question;
    }
}
