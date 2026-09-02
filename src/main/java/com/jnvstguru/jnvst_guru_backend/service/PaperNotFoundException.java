package com.jnvstguru.jnvst_guru_backend.service;

public class PaperNotFoundException extends RuntimeException {
    public PaperNotFoundException(Long paperId) {
        super("Paper not found: " + paperId);
    }
}
