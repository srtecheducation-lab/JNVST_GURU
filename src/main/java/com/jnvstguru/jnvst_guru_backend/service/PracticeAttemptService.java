package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.api.dto.*;
import com.jnvstguru.jnvst_guru_backend.domain.*;
import com.jnvstguru.jnvst_guru_backend.repository.*;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PracticeAttemptService {
    private static final int SET_SIZE = 20;
    private static final Logger log = LoggerFactory.getLogger(PracticeAttemptService.class);
    private final ApplicationUserService applicationUserService;
    private final ArithmeticQuestionRepository questionRepository;
    private final MatQuestionRepository matQuestionRepository;
    private final MatQuestionExplanationRepository matQuestionExplanationRepository;
    private final PracticeAttemptRepository attemptRepository;
    private final PracticeAttemptAnswerRepository answerRepository;
    private final EntityManager entityManager;

    @Autowired
    public PracticeAttemptService(ApplicationUserService applicationUserService,
                                  ArithmeticQuestionRepository questionRepository,
                                  MatQuestionRepository matQuestionRepository,
                                  MatQuestionExplanationRepository matQuestionExplanationRepository,
                                  PracticeAttemptRepository attemptRepository,
                                  PracticeAttemptAnswerRepository answerRepository,
                                  EntityManager entityManager) {
        this.applicationUserService = applicationUserService;
        this.questionRepository = questionRepository;
        this.matQuestionRepository = matQuestionRepository;
        this.matQuestionExplanationRepository = matQuestionExplanationRepository;
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.entityManager = entityManager;
    }

    public PracticeAttemptService(ApplicationUserService applicationUserService,
                                  ArithmeticQuestionRepository questionRepository,
                                  PracticeAttemptRepository attemptRepository,
                                  PracticeAttemptAnswerRepository answerRepository,
                                  EntityManager entityManager) {
        this(applicationUserService, questionRepository, null, null, attemptRepository, answerRepository, entityManager);
    }

    @Transactional
    public PracticeAttemptResponse submit(UUID authUserId, PracticeAttemptRequest request) {
        if (request.subject() == PracticeSubject.MAT) {
            return submitMat(authUserId, request);
        }
        validateSelection(request.practiceMode(), request.subject(), request.topic(),
                request.difficulty(), request.page());
        UserEntity user = applicationUserService.findByAuthUserId(authUserId);
        Map<Long, PracticeAttemptRequest.AnswerRequest> submitted = normalizeAnswers(request.answers());
        List<ArithmeticQuestionRepository.PracticeQuestionProjection> questions =
                findSet(request.topic(), request.difficulty(), request.page(), submitted.keySet());
        Set<Long> questionIds = questions.stream().map(ArithmeticQuestionRepository.PracticeQuestionProjection::getQuestionId).collect(Collectors.toSet());
        if (!questionIds.containsAll(submitted.keySet())) {
            Set<Long> rejectedIds = new LinkedHashSet<>(submitted.keySet());
            rejectedIds.removeAll(questionIds);
            log.warn("[PRACTICE_ATTEMPT] Submitted IDs outside resolved set: {}", rejectedIds);
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

    private PracticeAttemptResponse submitMat(UUID authUserId, PracticeAttemptRequest request) {
        validateMatSelection(request.practiceMode(), request.subject(), request.topicId(),
                request.topic(), request.difficulty(), request.page());
        UserEntity user = applicationUserService.findByAuthUserId(authUserId);
        Map<Long, PracticeAttemptRequest.AnswerRequest> submitted = normalizeAnswers(request.answers());
        Page<MatQuestionEntity> page = request.practiceMode() == PracticeMode.TOPIC
                ? matQuestionRepository.findByTopicIdAndDifficultyAndActiveTrueOrderBySortOrderAscIdAsc(
                        request.topicId(), request.difficulty().name(), PageRequest.of(request.page(), SET_SIZE))
                : matQuestionRepository.findByDifficultyAndActiveTrueOrderBySortOrderAscIdAsc(
                        request.difficulty().name(), PageRequest.of(request.page(), SET_SIZE));
        List<MatQuestionEntity> questions = page.getContent();
        Set<Long> questionIds = questions.stream().map(MatQuestionEntity::getId).collect(Collectors.toSet());
        if (!questionIds.containsAll(submitted.keySet())) {
            throw new IllegalArgumentException("One or more submitted MAT question IDs do not belong to this practice set.");
        }
        PracticeAttemptEntity attempt = new PracticeAttemptEntity();
        attempt.setUser(user);
        attempt.setPracticeMode(request.practiceMode());
        attempt.setSubject(PracticeSubject.MAT);
        attempt.setTopicId(request.topicId());
        attempt.setDifficulty(request.difficulty());
        attempt.setPageNumber(request.page());
        attempt.setQuestionCount(questions.size());
        List<PracticeAttemptAnswerEntity> answers = new ArrayList<>();
        int correct = 0;
        int unanswered = 0;
        for (MatQuestionEntity question : questions) {
            PracticeAttemptRequest.AnswerRequest submittedAnswer = submitted.get(question.getId());
            String selected = submittedAnswer == null ? null : normalizeOption(submittedAnswer.selectedOption());
            boolean isCorrect = selected != null && selected.equals(question.getCorrectOption());
            if (selected == null) unanswered++;
            else if (isCorrect) correct++;
            PracticeAttemptAnswerEntity answer = new PracticeAttemptAnswerEntity();
            answer.setAttempt(attempt);
            answer.setQuestionSource(PracticeQuestionSource.MAT);
            answer.setMatQuestionId(question.getId());
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
                .mapToObj(i -> "(:attempt" + i + ", :question" + i + ", :matQuestion" + i
                        + ", :source" + i + ", :selected" + i + ", :correct" + i + ", :isCorrect" + i + ")")
                .collect(Collectors.joining(", "));
        var query = entityManager.createNativeQuery("""
                insert into application.practice_attempt_answers
                    (attempt_id, question_id, mat_question_id, question_source, selected_option, correct_option, is_correct)
                values """ + values);
        for (int i = 0; i < answers.size(); i++) {
            PracticeAttemptAnswerEntity answer = answers.get(i);
            query.setParameter("attempt" + i, attemptId);
            query.setParameter("question" + i, answer.getQuestionId());
            query.setParameter("matQuestion" + i, answer.getMatQuestionId());
            query.setParameter("source" + i, answer.getQuestionSource().name());
            query.setParameter("selected" + i, answer.getSelectedOption());
            query.setParameter("correct" + i, answer.getCorrectOption());
            query.setParameter("isCorrect" + i, answer.isCorrect());
        }
        query.executeUpdate();
    }

    @Transactional(readOnly = true)
    public PracticeStatusResponse getStatus(UUID authUserId, PracticeMode mode, PracticeSubject subject,
                                            ArithmeticQuestionEnums.QuestionType topic,
                                            ArithmeticQuestionEnums.Difficulty difficulty, Long topicId) {
        if (subject == PracticeSubject.MAT) {
            validateMatSelection(mode, subject, topicId, topic, difficulty, 0);
            UserEntity user = applicationUserService.findByAuthUserId(authUserId);
            long total;
            Set<Integer> completed;
            if (mode == PracticeMode.TOPIC) {
                total = matQuestionRepository.countByActiveTrueAndTopicIdAndDifficulty(topicId, difficulty.name());
                completed = new HashSet<>(attemptRepository.findCompletedMatPages(
                        user, mode, subject, topicId, difficulty));
            } else {
                total = matQuestionRepository.countByActiveTrueAndDifficulty(difficulty.name());
                completed = new HashSet<>(attemptRepository.findCompletedMatSubjectPages(
                        user, mode, subject, difficulty));
            }
            return buildStatus(mode, subject, difficulty, topicId, total, completed);
        }

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
        return new PracticeStatusResponse(mode, subject, mode == PracticeMode.TOPIC ? topic : null,
                difficulty, sets, null);
    }

    public PracticeStatusResponse getStatus(UUID authUserId, PracticeMode mode, PracticeSubject subject,
                                            ArithmeticQuestionEnums.QuestionType topic,
                                            ArithmeticQuestionEnums.Difficulty difficulty) {
        return getStatus(authUserId, mode, subject, topic, difficulty, null);
    }

    @Transactional(readOnly = true)
    public PracticeAttemptResponse getLatest(UUID authUserId, PracticeMode mode, PracticeSubject subject,
                                             ArithmeticQuestionEnums.QuestionType topic,
                                             ArithmeticQuestionEnums.Difficulty difficulty, Integer page, Long topicId) {
        if (subject == PracticeSubject.MAT) {
            validateMatSelection(mode, subject, topicId, topic, difficulty, page);
            UserEntity user = applicationUserService.findByAuthUserId(authUserId);
            PracticeAttemptEntity attempt;
            if (mode == PracticeMode.TOPIC) {
                attempt = attemptRepository
                        .findFirstByUserAndPracticeModeAndSubjectAndTopicIdAndDifficultyAndPageNumberOrderBySubmittedAtDescIdDesc(
                                user, mode, subject, topicId, difficulty, page);
            } else {
                List<PracticeAttemptEntity> attempts = attemptRepository.findLatestMatSubject(
                        user, mode, subject, difficulty, page, PageRequest.of(0, 1));
                attempt = attempts.isEmpty() ? null : attempts.get(0);
            }
            if (attempt == null) throw new PracticeAttemptNotFoundException();
            List<PracticeAttemptAnswerEntity> answers = answerRepository.findByAttemptOrderById(attempt);
            String languageCode = preferredLanguageCode(attempt.getUser());
            Map<Long, String> explanations = loadExplanations(answers, languageCode);
            return toResponse(attempt, answers, explanations);
        }

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

    public PracticeAttemptResponse getLatest(UUID authUserId, PracticeMode mode, PracticeSubject subject,
                                             ArithmeticQuestionEnums.QuestionType topic,
                                             ArithmeticQuestionEnums.Difficulty difficulty, Integer page) {
        return getLatest(authUserId, mode, subject, topic, difficulty, page, null);
    }

    private PracticeStatusResponse buildStatus(PracticeMode mode, PracticeSubject subject,
                                               ArithmeticQuestionEnums.Difficulty difficulty, Long topicId,
                                               long total, Set<Integer> completed) {
        int setCount = (int) ((total + SET_SIZE - 1) / SET_SIZE);
        List<PracticeStatusResponse.SetStatus> sets = new ArrayList<>();
        for (int page = 0; page < setCount; page++) {
            int count = (int) Math.min(SET_SIZE, total - (long) page * SET_SIZE);
            sets.add(new PracticeStatusResponse.SetStatus(page, page + 1, count, completed.contains(page)));
        }
        return new PracticeStatusResponse(mode, subject, null, difficulty, sets, topicId);
    }

    private List<ArithmeticQuestionRepository.PracticeQuestionProjection> findSet(
            ArithmeticQuestionEnums.QuestionType topic,
            ArithmeticQuestionEnums.Difficulty difficulty, int page, Set<Long> submittedIds) {
        PageRequest pageRequest = PageRequest.of(page, SET_SIZE);
        List<ArithmeticQuestionRepository.PracticeQuestionProjection> english =
                questionRepository.findActivePracticeQuestions(topic, difficulty, pageRequest);
        if (submittedIds.isEmpty()) {
            return english;
        }
        if (containsAllIds(english, submittedIds)) {
            return english;
        }

        List<ArithmeticQuestionRepository.PracticeQuestionProjection> bengali =
                questionRepository.findActiveBengaliPracticeQuestions(topic, difficulty, pageRequest);
        return containsAllIds(bengali, submittedIds) ? bengali : english;
    }

    private boolean containsAllIds(
            List<ArithmeticQuestionRepository.PracticeQuestionProjection> questions,
            Set<Long> submittedIds) {
        return questions.stream()
                .map(ArithmeticQuestionRepository.PracticeQuestionProjection::getQuestionId)
                .collect(Collectors.toSet())
                .containsAll(submittedIds);
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
            throw new IllegalArgumentException("Unsupported practice subject.");
        }

        if (mode == PracticeMode.TOPIC && topic == null) {
            throw new IllegalArgumentException("topic is required for TOPIC practice.");
        }
        if (mode == PracticeMode.SUBJECT && topic != null) {
            throw new IllegalArgumentException("topic must be null for SUBJECT practice.");
        }
    }

    private void validateMatSelection(PracticeMode mode, PracticeSubject subject, Long topicId,
                                      ArithmeticQuestionEnums.QuestionType topic,
                                      ArithmeticQuestionEnums.Difficulty difficulty, Integer page) {
        boolean invalidTopicMode = mode == PracticeMode.TOPIC
                && (topicId == null || topicId < 1);
        boolean invalidSubjectMode = mode == PracticeMode.SUBJECT && topicId != null;
        if (subject != PracticeSubject.MAT || (mode != PracticeMode.TOPIC && mode != PracticeMode.SUBJECT)
                || invalidTopicMode || invalidSubjectMode || difficulty == null
                || page == null || page < 0 || topic != null) {
            throw new IllegalArgumentException(
                    "MAT practice requires practiceMode=SUBJECT or TOPIC, subject=MAT, "
                            + "difficulty, and a non-negative page; topicId is required for TOPIC.");
        }
    }

    private PracticeAttemptResponse toResponse(PracticeAttemptEntity attempt,
                                               List<PracticeAttemptAnswerEntity> answers) {
        return toResponse(attempt, answers, Map.of());
    }

    private PracticeAttemptResponse toResponse(PracticeAttemptEntity attempt,
                                               List<PracticeAttemptAnswerEntity> answers,
                                               Map<Long, String> explanations) {
        return new PracticeAttemptResponse(
                attempt.getId(), attempt.getPracticeMode(), attempt.getSubject(), attempt.getTopic(),
                attempt.getDifficulty(), attempt.getPageNumber(), attempt.getScore(), attempt.getQuestionCount(),
                attempt.getCorrectCount(), attempt.getWrongCount(), attempt.getUnansweredCount(),
                attempt.getSubmittedAt(), answers.stream()
                .map(answer -> new PracticeAttemptResponse.AnswerResponse(
                        answer.getQuestionId(), answer.getSelectedOption(),
                        answer.getCorrectOption(), answer.isCorrect(), answer.getMatQuestionId(),
                        explanations.get(answer.getMatQuestionId())))
                .toList(), attempt.getTopicId());
    }

    private Map<Long, String> loadExplanations(List<PracticeAttemptAnswerEntity> answers, String languageCode) {
        Set<Long> matQuestionIds = answers.stream()
                .map(PracticeAttemptAnswerEntity::getMatQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (matQuestionIds.isEmpty()) {
            return Map.of();
        }
        return matQuestionExplanationRepository
                .findByMatQuestionIdInAndLanguageCode(matQuestionIds, languageCode)
                .stream()
                .collect(Collectors.toMap(
                        explanation -> explanation.getMatQuestion().getId(),
                        MatQuestionExplanationEntity::getExplanation));
    }

    private String preferredLanguageCode(UserEntity user) {
        StudentProfileEntity profile = user.getStudentProfile();
        if (profile == null || profile.getPreferredLanguage() == null
                || profile.getPreferredLanguage().isBlank()) {
            return "en";
        }
        return profile.getPreferredLanguage().trim().toLowerCase(Locale.ROOT).equals("bn")
                ? "bn" : "en";
    }
}
