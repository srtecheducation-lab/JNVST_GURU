package com.jnvstguru.jnvst_guru_backend.api;

import com.jnvstguru.jnvst_guru_backend.api.dto.*;
import com.jnvstguru.jnvst_guru_backend.service.MatQuestionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class MatQuestionController {
    private final MatQuestionService service;
    public MatQuestionController(MatQuestionService service) { this.service = service; }

    @GetMapping("/student/mat-topics")
    public Page<MatTopicResponse> studentTopics(
            @RequestParam(required = false, defaultValue = "en") String language,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.studentTopics(language, pageable);
    }

    @GetMapping("/student/mat-questions")
    public Page<StudentMatQuestionResponse> studentQuestions(
            @RequestParam(required = false) Long topicId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.studentQuestions(topicId, pageable);
    }

    @GetMapping("/mat-questions")
    public Page<MatQuestionResponse> questions(
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.adminQuestions(topicId, active, pageable);
    }

    @PostMapping("/mat-questions")
    public ResponseEntity<MatQuestionResponse> create(@Valid @RequestBody MatQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/mat-questions/{id}")
    public MatQuestionResponse update(@PathVariable Long id, @Valid @RequestBody MatQuestionRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/mat-questions/{id}")
    public MatQuestionResponse archive(@PathVariable Long id) { return service.archive(id); }
}
