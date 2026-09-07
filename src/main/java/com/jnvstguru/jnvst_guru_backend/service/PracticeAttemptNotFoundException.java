package com.jnvstguru.jnvst_guru_backend.service;

public class PracticeAttemptNotFoundException extends RuntimeException {
    public PracticeAttemptNotFoundException() {
        super("No completed attempt exists for the requested practice set.");
    }
}
