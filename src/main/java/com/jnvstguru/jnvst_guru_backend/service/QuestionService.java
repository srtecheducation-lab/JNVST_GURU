package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.domain.QuestionEntity;
import com.jnvstguru.jnvst_guru_backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Transactional(readOnly = true)
    public QuestionEntity getQuestionById(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long questionId) {
        return questionRepository.existsById(questionId);
    }
}
