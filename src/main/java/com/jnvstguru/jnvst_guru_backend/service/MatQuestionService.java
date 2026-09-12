package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.api.dto.*;
import com.jnvstguru.jnvst_guru_backend.domain.MatQuestionEntity;
import com.jnvstguru.jnvst_guru_backend.domain.MatTopicEntity;
import com.jnvstguru.jnvst_guru_backend.repository.MatQuestionRepository;
import com.jnvstguru.jnvst_guru_backend.repository.MatTopicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatQuestionService {
    private final MatQuestionRepository questions;
    private final MatTopicRepository topics;

    public MatQuestionService(MatQuestionRepository questions, MatTopicRepository topics) {
        this.questions = questions;
        this.topics = topics;
    }

    @Transactional(readOnly = true)
    public Page<StudentMatQuestionResponse> studentQuestions(Long topicId, String difficulty, Pageable pageable) {
        validatePage(pageable);
        String selectedDifficulty = difficulty == null || difficulty.isBlank() ? null : difficulty.trim();
        Page<MatQuestionEntity> page;
        if (topicId == null && selectedDifficulty == null) {
            page = questions.findByActiveTrueOrderBySortOrderAscIdAsc(pageable);
        } else if (topicId == null) {
            page = questions.findByDifficultyAndActiveTrueOrderBySortOrderAscIdAsc(selectedDifficulty, pageable);
        } else if (selectedDifficulty == null) {
            page = questions.findByTopicIdAndActiveTrueOrderBySortOrderAscIdAsc(topicId, pageable);
        } else {
            page = questions.findByTopicIdAndDifficultyAndActiveTrueOrderBySortOrderAscIdAsc(
                    topicId, selectedDifficulty, pageable);
        }
        return page.map(this::toStudentResponse);
    }

    @Transactional(readOnly = true)
    public Page<MatTopicResponse> studentTopics(String language, Pageable pageable) {
        validatePage(pageable);
        String selectedLanguage = language == null || language.isBlank() ? "en" : language.trim().toLowerCase();
        return topics.findActiveStudentTopics(selectedLanguage, pageable)
                .map(t -> new MatTopicResponse(t.getId(), t.getCode(), t.getSortOrder(), t.getName(), t.getDescription()));
    }

    @Transactional(readOnly = true)
    public Page<MatQuestionResponse> adminQuestions(Long topicId, Boolean active, Pageable pageable) {
        validatePage(pageable);
        Page<MatQuestionEntity> page;
        if (Boolean.TRUE.equals(active)) {
            page = topicId == null ? questions.findByActiveTrueOrderBySortOrderAscIdAsc(pageable)
                    : questions.findByTopicIdAndActiveTrueOrderBySortOrderAscIdAsc(topicId, pageable);
        } else {
            page = topicId == null ? questions.findByOrderBySortOrderAscIdAsc(pageable)
                    : questions.findByTopicIdOrderBySortOrderAscIdAsc(topicId, pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional
    public MatQuestionResponse create(MatQuestionRequest request) {
        MatQuestionEntity entity = new MatQuestionEntity();
        apply(entity, request);
        return toResponse(questions.save(entity));
    }

    @Transactional
    public MatQuestionResponse update(Long id, MatQuestionRequest request) {
        MatQuestionEntity entity = questions.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MAT question not found: " + id));
        apply(entity, request);
        return toResponse(questions.save(entity));
    }

    @Transactional
    public MatQuestionResponse archive(Long id) {
        MatQuestionEntity entity = questions.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MAT question not found: " + id));
        entity.setActive(false);
        return toResponse(questions.save(entity));
    }

    private void apply(MatQuestionEntity entity, MatQuestionRequest request) {
        MatTopicEntity topic = topics.findById(request.topicId())
                .orElseThrow(() -> new IllegalArgumentException("MAT topic not found: " + request.topicId()));
        entity.setTopic(topic);
        entity.setQuestionImageUrl(request.questionImageUrl().trim());
        entity.setOptionAImageUrl(request.optionAImageUrl().trim());
        entity.setOptionBImageUrl(request.optionBImageUrl().trim());
        entity.setOptionCImageUrl(request.optionCImageUrl().trim());
        entity.setOptionDImageUrl(request.optionDImageUrl().trim());
        entity.setCorrectOption(request.correctOption().trim().toUpperCase());
        entity.setDifficulty(request.difficulty() == null ? null : request.difficulty().trim());
        entity.setActive(request.active() == null || request.active());
        entity.setSortOrder(request.sortOrder());
    }

    private MatQuestionResponse toResponse(MatQuestionEntity q) {
        return new MatQuestionResponse(q.getId(), q.getTopic().getId(), q.getQuestionImageUrl(),
                q.getOptionAImageUrl(), q.getOptionBImageUrl(), q.getOptionCImageUrl(), q.getOptionDImageUrl(),
                q.getCorrectOption(), q.getDifficulty(), q.isActive(), q.getSortOrder(), q.getCreatedAt(), q.getUpdatedAt());
    }

    private StudentMatQuestionResponse toStudentResponse(MatQuestionEntity q) {
        return new StudentMatQuestionResponse(q.getId(), q.getTopic().getId(), q.getQuestionImageUrl(),
                q.getOptionAImageUrl(), q.getOptionBImageUrl(), q.getOptionCImageUrl(), q.getOptionDImageUrl(),
                q.getDifficulty());
    }

    private void validatePage(Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("page must be non-negative and size must not exceed 100");
        }
    }
}
