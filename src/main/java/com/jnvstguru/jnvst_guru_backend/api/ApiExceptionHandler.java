package com.jnvstguru.jnvst_guru_backend.api;

import com.jnvstguru.jnvst_guru_backend.service.ArithmeticQuestionNotFoundException;
import com.jnvstguru.jnvst_guru_backend.service.StudentProfileAlreadyExistsException;
import com.jnvstguru.jnvst_guru_backend.service.QuestionNotFoundException;
import com.jnvstguru.jnvst_guru_backend.service.PaperNotFoundException;
import com.jnvstguru.jnvst_guru_backend.service.PracticeAttemptNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        log.warn("[ERROR] Validation failed for request path={} message={}", path, ex.getMessage());
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", OffsetDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Validation failed");
        error.put("message", ex.getBindingResult().getFieldError() == null
                ? "Request validation failed"
                : ex.getBindingResult().getFieldError().getDefaultMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        log.warn("[ERROR] Constraint violation for request path={} message={}", path, ex.getMessage());
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", OffsetDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Constraint violation");
        error.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(StudentProfileAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleStudentProfileAlreadyExists(StudentProfileAlreadyExistsException ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        log.warn("[STUDENT_PROFILE] Duplicate profile conflict for path={} message={}", path, ex.getMessage());
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", OffsetDateTime.now());
        error.put("status", HttpStatus.CONFLICT.value());
        error.put("error", "Conflict");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ArithmeticQuestionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleArithmeticQuestionNotFound(ArithmeticQuestionNotFoundException ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        log.warn("[ARITHMETIC_QUESTION] Not found for path={} message={}", path, ex.getMessage());
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", OffsetDateTime.now());
        error.put("status", HttpStatus.NOT_FOUND.value());
        error.put("error", "Not Found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler({QuestionNotFoundException.class, PaperNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleQuestionBankNotFound(RuntimeException ex, HttpServletRequest request) {
        log.warn("[QUESTION_BANK] Not found for path={} message={}", request.getRequestURI(), ex.getMessage());
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", OffsetDateTime.now());
        error.put("status", HttpStatus.NOT_FOUND.value());
        error.put("error", "Not Found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(PracticeAttemptNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePracticeAttemptNotFound(PracticeAttemptNotFoundException ex, HttpServletRequest request) {
        log.warn("[PRACTICE_ATTEMPT] Not found for path={} message={}", request.getRequestURI(), ex.getMessage());
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", OffsetDateTime.now());
        error.put("status", HttpStatus.NOT_FOUND.value());
        error.put("error", "Not Found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        log.warn("[ERROR] Malformed request body for path={} message={}", path, ex.getMessage());
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", OffsetDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Bad request");
        error.put("message", "Request body is invalid or contains unsupported enum values");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("[ERROR] Invalid request parameter for path={} parameter={} value={}",
                request.getRequestURI(), ex.getName(), ex.getValue());
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", OffsetDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Bad request");
        error.put("message", "Invalid value for request parameter: " + ex.getName());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        log.warn("[ERROR] Bad request for path={} message={}", path, ex.getMessage());
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", OffsetDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Bad request");
        error.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        log.warn("[ERROR] Unauthorized request for path={} message={}", path, ex.getMessage());
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", OffsetDateTime.now());
        error.put("status", HttpStatus.UNAUTHORIZED.value());
        error.put("error", "Unauthorized");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        log.error("[ERROR] Unexpected exception for request path={}", path, ex);

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", OffsetDateTime.now());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Internal server error");
        error.put("message", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
