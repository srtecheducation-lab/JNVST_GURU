package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.api.dto.ArithmeticQuestionRequest;
import com.jnvstguru.jnvst_guru_backend.api.dto.ArithmeticQuestionResponse;
import com.jnvstguru.jnvst_guru_backend.api.dto.StudentArithmeticQuestionResponse;
import com.jnvstguru.jnvst_guru_backend.domain.ArithmeticQuestionEntity;
import com.jnvstguru.jnvst_guru_backend.domain.ArithmeticQuestionEnums;
import com.jnvstguru.jnvst_guru_backend.repository.ArithmeticQuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArithmeticQuestionService {
    private static final Logger log = LoggerFactory.getLogger(ArithmeticQuestionService.class);

    private final ArithmeticQuestionRepository arithmeticQuestionRepository;

    public ArithmeticQuestionService(ArithmeticQuestionRepository arithmeticQuestionRepository) {
        this.arithmeticQuestionRepository = arithmeticQuestionRepository;
    }

    @Transactional(readOnly = true)
    public Page<StudentArithmeticQuestionResponse> getStudentQuestions(
            ArithmeticQuestionEnums.QuestionType questionType,
            ArithmeticQuestionEnums.Difficulty difficulty,
            Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("page must be non-negative and size must not exceed 100");
        }

        return arithmeticQuestionRepository.findActiveStudentQuestions(questionType, difficulty, pageable)
                .map(question -> new StudentArithmeticQuestionResponse(
                        question.getQuestionId(),
                        question.getQuestionType(),
                        question.getQuestionText(),
                        question.getOptionA(),
                        question.getOptionB(),
                        question.getOptionC(),
                        question.getOptionD(),
                        question.getDifficulty()));
    }

    @Transactional(readOnly = true)
    public Page<ArithmeticQuestionResponse> getQuestions(
            ArithmeticQuestionEnums.QuestionType questionType,
            ArithmeticQuestionEnums.Difficulty difficulty,
            ArithmeticQuestionEnums.Status status,
            Pageable pageable) {

        Page<ArithmeticQuestionEntity> page;
        if (questionType != null && difficulty != null && status != null) {
            page = arithmeticQuestionRepository.findByQuestionTypeAndDifficultyAndStatusOrderByCreatedAtDesc(questionType, difficulty, status, pageable);
        } else if (questionType != null && difficulty != null) {
            page = arithmeticQuestionRepository.findByQuestionTypeAndDifficultyAndStatusOrderByCreatedAtDesc(questionType, difficulty, ArithmeticQuestionEnums.Status.ACTIVE, pageable);
        } else if (questionType != null && status != null) {
            page = arithmeticQuestionRepository.findByQuestionTypeAndStatusOrderByCreatedAtDesc(questionType, status, pageable);
        } else if (difficulty != null && status != null) {
            page = arithmeticQuestionRepository.findByDifficultyAndStatusOrderByCreatedAtDesc(difficulty, status, pageable);
        } else if (questionType != null) {
            page = arithmeticQuestionRepository.findByQuestionTypeAndStatusOrderByCreatedAtDesc(questionType, ArithmeticQuestionEnums.Status.ACTIVE, pageable);
        } else if (difficulty != null) {
            page = arithmeticQuestionRepository.findByDifficultyAndStatusOrderByCreatedAtDesc(difficulty, ArithmeticQuestionEnums.Status.ACTIVE, pageable);
        } else if (status != null) {
            page = arithmeticQuestionRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            page = arithmeticQuestionRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ArithmeticQuestionResponse getQuestion(Long id) {
        ArithmeticQuestionEntity question = arithmeticQuestionRepository.findById(id)
                .orElseThrow(() -> new ArithmeticQuestionNotFoundException(id));
        return toResponse(question);
    }

    @Transactional
    public ArithmeticQuestionResponse createQuestion(ArithmeticQuestionRequest request) {
        ArithmeticQuestionEntity entity = new ArithmeticQuestionEntity();
        entity.setQuestionText(request.questionText().trim());
        entity.setQuestionType(request.questionType());
        entity.setOptionA(request.optionA().trim());
        entity.setOptionB(request.optionB().trim());
        entity.setOptionC(request.optionC().trim());
        entity.setOptionD(request.optionD().trim());
        entity.setCorrectOption(request.correctOption().trim().toUpperCase());
        entity.setDifficulty(request.difficulty());
        entity.setExplanation(request.explanation() == null ? null : request.explanation().trim());
        entity.setStatus(request.status());

        ArithmeticQuestionEntity saved = arithmeticQuestionRepository.save(entity);
        log.info("[ARITHMETIC_QUESTION] Created arithmetic question id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public ArithmeticQuestionResponse updateQuestion(Long id, ArithmeticQuestionRequest request) {
        ArithmeticQuestionEntity question = arithmeticQuestionRepository.findById(id)
                .orElseThrow(() -> new ArithmeticQuestionNotFoundException(id));

        question.setQuestionText(request.questionText().trim());
        question.setQuestionType(request.questionType());
        question.setOptionA(request.optionA().trim());
        question.setOptionB(request.optionB().trim());
        question.setOptionC(request.optionC().trim());
        question.setOptionD(request.optionD().trim());
        question.setCorrectOption(request.correctOption().trim().toUpperCase());
        question.setDifficulty(request.difficulty());
        question.setExplanation(request.explanation() == null ? null : request.explanation().trim());
        question.setStatus(request.status());

        ArithmeticQuestionEntity updated = arithmeticQuestionRepository.save(question);
        log.info("[ARITHMETIC_QUESTION] Updated arithmetic question id={}", updated.getId());
        return toResponse(updated);
    }

    @Transactional
    public ArithmeticQuestionResponse deleteQuestion(Long id) {
        ArithmeticQuestionEntity question = arithmeticQuestionRepository.findById(id)
                .orElseThrow(() -> new ArithmeticQuestionNotFoundException(id));
        question.setStatus(ArithmeticQuestionEnums.Status.ARCHIVED);
        ArithmeticQuestionEntity archived = arithmeticQuestionRepository.save(question);
        log.info("[ARITHMETIC_QUESTION] Archived arithmetic question id={}", archived.getId());
        return toResponse(archived);
    }

    private ArithmeticQuestionResponse toResponse(ArithmeticQuestionEntity entity) {
        return new ArithmeticQuestionResponse(
                entity.getId(),
                entity.getQuestionText(),
                entity.getQuestionType(),
                entity.getOptionA(),
                entity.getOptionB(),
                entity.getOptionC(),
                entity.getOptionD(),
                entity.getCorrectOption(),
                entity.getDifficulty(),
                entity.getExplanation(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
