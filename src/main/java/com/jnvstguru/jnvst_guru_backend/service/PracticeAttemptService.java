package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.api.dto.*;
import com.jnvstguru.jnvst_guru_backend.domain.*;
import com.jnvstguru.jnvst_guru_backend.repository.*;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PracticeAttemptService {
    private static final int SET_SIZE = 20;
    private final ApplicationUserService applicationUserService;
    private final ArithmeticQuestionRepository questionRepository;
    private final PracticeAttemptRepository attemptRepository;
    private final PracticeAttemptAnswerRepository answerRepository;
    private final EntityManager entityManager;

    public PracticeAttemptService(ApplicationUserService applicationUserService,
                                  ArithmeticQuestionRepository questionRepository,
                                  PracticeAttemptRepository attemptRepository,
                                  PracticeAttemptAnswerRepository answerRepository,
                                  EntityManager entityManager) {
        this.applicationUserService = applicationUserService;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public PracticeAttemptResponse submit(UUID authUserId, PracticeAttemptRequest request) {
        validateSelection(request.practiceMode(), request.subject(), request.topic(),
                request.difficulty(), request.page());
        UserEntity user = applicationUserService.findByAuthUserId(authUserId);
        List<ArithmeticQuestionRepository.PracticeQuestionProjection> questions =
                findSet(request.topic(), request.difficulty(), request.page());
        Map<Long, PracticeAttemptRequest.AnswerRequest> submitted = normalizeAnswers(request.answers());
        Set<Long> questionIds = questions.stream().map(ArithmeticQuestionRepository.PracticeQuestionProjection::getQuestionId).collect(Collectors.toSet());
        if (!questionIds.containsAll(submitted.keySet())) {
            throw new IllegalArgumentException("One or more submitted question IDs do not belong to this practice set.");
        }

        int correct = 0;
        int unanswered = 0;
        List<PracticeAttemptAnswerEntity> answers = new ArrayList<>();
        PracticeAttemptEntity attempt = new PracticeAttemptEntity();
        attempt.setUser(user);
        attempt.setPracticeMode(request.practiceMode());
        attempt.setSubject(request.subject());
        attempt.setTopic(request.topic());
        attempt.setDifficulty(request.difficulty());
        attempt.setPageNumber(request.page());
        attempt.setQuestionCount(questions.size());

        for (ArithmeticQuestionRepository.PracticeQuestionProjection question : questions) {
            PracticeAttemptRequest.AnswerRequest submittedAnswer = submitted.get(question.getQuestionId());
            String selected = submittedAnswer == null ? null : normalizeOption(submittedAnswer.selectedOption());
            boolean isCorrect = selected != null && selected.equals(question.getCorrectOption());
            if (selected == null) {
                unanswered++;
            } else if (isCorrect) {
                correct++;
            }
            PracticeAttemptAnswerEntity answer = new PracticeAttemptAnswerEntity();
            answer.setAttempt(attempt);
            answer.setQuestionId(question.getQuestionId());
            answer.setSelectedOption(selected);
            answer.setCorrectOption(question.getCorrectOption());
            answer.setCorrect(isCorrect);
            answers.add(answer);
        }
        attempt.setScore(correct);
        attempt.setCorrectCount(correct);
        attempt.setUnansweredCount(unanswered);
        attempt.setWrongCount(questions.size() - correct - unanswered);
        PracticeAttemptEntity saved = attemptRepository.save(attempt);
        saveAnswersBulk(saved.getId(), answers);
        return toResponse(saved, answers);
    }

    private void saveAnswersBulk(Long attemptId, List<PracticeAttemptAnswerEntity> answers) {
        if (answers.isEmpty()) {
            return;
        }
        String values = java.util.stream.IntStream.range(0, answers.size())
                .mapToObj(i -> "(:attempt" + i + ", :question" + i + ", :selected" + i
                        + ", :correct" + i + ", :isCorrect" + i + ")")
                .collect(Collectors.joining(", "));
        var query = entityManager.createNativeQuery("""
                insert into application.practice_attempt_answers
                    (attempt_id, question_id, selected_option, correct_option, is_correct)
                values """ + values);
        for (int i = 0; i < answers.size(); i++) {
            PracticeAttemptAnswerEntity answer = answers.get(i);
            query.setParameter("attempt" + i, attemptId);
            query.setParameter("question" + i, answer.getQuestionId());
            query.setParameter("selected" + i, answer.getSelectedOption());
            query.setParameter("correct" + i, answer.getCorrectOption());
            query.setParameter("isCorrect" + i, answer.isCorrect());
        }
        query.executeUpdate();
    }

    @Transactional(readOnly = true)
    public PracticeStatusResponse getStatus(UUID authUserId, PracticeMode mode, PracticeSubject subject,
                                            ArithmeticQuestionEnums.QuestionType topic,
                                            ArithmeticQuestionEnums.Difficulty difficulty) {
        validateSelection(mode, subject, topic, difficulty, 0);
        UserEntity user = applicationUserService.findByAuthUserId(authUserId);
        long total = questionRepository.countActivePracticeQuestions(mode == PracticeMode.TOPIC ? topic : null, difficulty);
        Set<Integer> completed = new HashSet<>(attemptRepository.findCompletedPages(
                user, mode, subject, mode == PracticeMode.TOPIC ? topic : null, difficulty));
        int setCount = (int) ((total + SET_SIZE - 1) / SET_SIZE);
        List<PracticeStatusResponse.SetStatus> sets = new ArrayList<>();
        for (int page = 0; page < setCount; page++) {
            int count = (int) Math.min(SET_SIZE, total - (long) page * SET_SIZE);
            sets.add(new PracticeStatusResponse.SetStatus(page, page + 1, count, completed.contains(page)));
        }
        return new PracticeStatusResponse(mode, subject, mode == PracticeMode.TOPIC ? topic : null, difficulty, sets);
    }

    @Transactional(readOnly = true)
    public PracticeAttemptResponse getLatest(UUID authUserId, PracticeMode mode, PracticeSubject subject,
                                             ArithmeticQuestionEnums.QuestionType topic,
                                             ArithmeticQuestionEnums.Difficulty difficulty, Integer page) {
        validateSelection(mode, subject, topic, difficulty, page);
        UserEntity user = applicationUserService.findByAuthUserId(authUserId);
        PracticeAttemptEntity attempt = attemptRepository
                .findFirstByUserAndPracticeModeAndSubjectAndTopicAndDifficultyAndPageNumberOrderBySubmittedAtDescIdDesc(
                        user, mode, subject, mode == PracticeMode.TOPIC ? topic : null, difficulty, page);
        if (attempt == null) {
            throw new PracticeAttemptNotFoundException();
        }
        return toResponse(attempt, answerRepository.findByAttemptOrderByQuestionId(attempt));
    }

    private List<ArithmeticQuestionRepository.PracticeQuestionProjection> findSet(
            ArithmeticQuestionEnums.QuestionType topic,
            ArithmeticQuestionEnums.Difficulty difficulty, int page) {
        return questionRepository.findActivePracticeQuestions(
                topic, difficulty, PageRequest.of(page, SET_SIZE));
    }

    private Map<Long, PracticeAttemptRequest.AnswerRequest> normalizeAnswers(
            List<PracticeAttemptRequest.AnswerRequest> requests) {
        if (requests == null) {
            return Map.of();
        }
        Map<Long, PracticeAttemptRequest.AnswerRequest> answers = new LinkedHashMap<>();
        for (PracticeAttemptRequest.AnswerRequest answer : requests) {
            if (answer == null || answer.questionId() == null) {
                throw new IllegalArgumentException("Every answer must include a questionId.");
            }
            normalizeOption(answer.selectedOption());
            if (answers.put(answer.questionId(), answer) != null) {
                throw new IllegalArgumentException("A question may be submitted only once.");
            }
        }
        return answers;
    }

    private String normalizeOption(String option) {
        if (option == null || option.isBlank()) {
            return null;
        }
        String normalized = option.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("A", "B", "C", "D").contains(normalized)) {
            throw new IllegalArgumentException("selectedOption must be A, B, C, D, or null.");
        }
        return normalized;
    }

    private void validateSelection(PracticeMode mode, PracticeSubject subject,
                                   ArithmeticQuestionEnums.QuestionType topic,
                                   ArithmeticQuestionEnums.Difficulty difficulty, Integer page) {
        if (mode == null || subject == null || difficulty == null || page == null || page < 0) {
            throw new IllegalArgumentException("practiceMode, subject, difficulty, and a non-negative page are required.");
        }
        if (subject != PracticeSubject.ARITHMETIC) {
            throw new IllegalArgumentException("Only ARITHMETIC practice is currently supported.");
        }
        if (mode == PracticeMode.TOPIC && topic == null) {
            throw new IllegalArgumentException("topic is required for TOPIC practice.");
        }
        if (mode == PracticeMode.SUBJECT && topic != null) {
            throw new IllegalArgumentException("topic must be null for SUBJECT practice.");
        }
    }

    private PracticeAttemptResponse toResponse(PracticeAttemptEntity attempt,
                                               List<PracticeAttemptAnswerEntity> answers) {
        return new PracticeAttemptResponse(
                attempt.getId(), attempt.getPracticeMode(), attempt.getSubject(), attempt.getTopic(),
                attempt.getDifficulty(), attempt.getPageNumber(), attempt.getScore(), attempt.getQuestionCount(),
                attempt.getCorrectCount(), attempt.getWrongCount(), attempt.getUnansweredCount(),
                attempt.getSubmittedAt(), answers.stream()
                .map(answer -> new PracticeAttemptResponse.AnswerResponse(
                        answer.getQuestionId(), answer.getSelectedOption(),
                        answer.getCorrectOption(), answer.isCorrect()))
                .toList());
    }
}
