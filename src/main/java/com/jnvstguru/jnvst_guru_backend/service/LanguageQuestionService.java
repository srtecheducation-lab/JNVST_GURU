package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.api.dto.StudentLanguagePassageResponse;
import com.jnvstguru.jnvst_guru_backend.domain.LanguageCode;
import com.jnvstguru.jnvst_guru_backend.domain.LanguageQuestionIndependentEntity;
import com.jnvstguru.jnvst_guru_backend.repository.LanguagePassageRepository;
import com.jnvstguru.jnvst_guru_backend.repository.LanguageQuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LanguageQuestionService {
    private final LanguageQuestionRepository questionRepository;
    private final LanguagePassageRepository passageRepository;

    public LanguageQuestionService(
            LanguageQuestionRepository questionRepository,
            LanguagePassageRepository passageRepository) {
        this.questionRepository = questionRepository;
        this.passageRepository = passageRepository;
    }

    @Transactional(readOnly = true)
    public Page<StudentLanguagePassageResponse> getStudentPassages(
            LanguageCode language, Pageable pageable) {
        if (language == null) {
            throw new IllegalArgumentException("language is required.");
        }
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException("page and size must be positive.");
        }
        Page<com.jnvstguru.jnvst_guru_backend.domain.LanguagePassageEntity> passages =
                passageRepository.findActivePassagesByLanguage(language.code(), pageable);
        List<Long> passageIds = passages.getContent().stream()
                .map(com.jnvstguru.jnvst_guru_backend.domain.LanguagePassageEntity::getId)
                .toList();
        Map<Long, List<LanguageQuestionIndependentEntity>> questionsByPassage =
                passageIds.isEmpty()
                        ? Map.of()
                        : questionRepository.findByPassageIdInAndActiveTrueOrderByQuestionNumberAscIdAsc(
                                passageIds).stream()
                        .collect(Collectors.groupingBy(
                                question -> question.getPassage().getId(),
                                Collectors.toList()));

        return passages.map(passage -> new StudentLanguagePassageResponse(
                passage.getId(),
                passage.getPassageNumber(),
                passage.getPassageText(),
                questionsByPassage.getOrDefault(passage.getId(), List.of()).stream()
                        .map(this::toQuestionResponse)
                        .toList()));
    }

    private StudentLanguagePassageResponse.Question toQuestionResponse(
            LanguageQuestionIndependentEntity question) {
        return new StudentLanguagePassageResponse.Question(
                question.getId(),
                question.getQuestionNumber(),
                question.getQuestionText(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD());
    }
}
