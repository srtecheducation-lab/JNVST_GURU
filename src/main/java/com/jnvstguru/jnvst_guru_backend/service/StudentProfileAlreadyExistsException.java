package com.jnvstguru.jnvst_guru_backend.service;

public class StudentProfileAlreadyExistsException extends RuntimeException {
    public StudentProfileAlreadyExistsException() {
        super("Student profile already exists for this user.");
    }
}
