package com.jnvstguru.jnvst_guru_backend.service;

public class ArithmeticQuestionNotFoundException extends RuntimeException {
    public ArithmeticQuestionNotFoundException(Long id) {
        super("Arithmetic question not found with id=" + id);
    }
}
