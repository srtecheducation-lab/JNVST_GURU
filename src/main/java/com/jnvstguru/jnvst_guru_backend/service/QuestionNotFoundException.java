package com.jnvstguru.jnvst_guru_backend.service;

public class QuestionNotFoundException extends RuntimeException {
    public QuestionNotFoundException(Long questionId) {
        super("Question not found: " + questionId);
    }
}
