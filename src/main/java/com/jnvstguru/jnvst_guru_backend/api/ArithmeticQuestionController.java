package com.jnvstguru.jnvst_guru_backend.api;

import com.jnvstguru.jnvst_guru_backend.api.dto.ArithmeticQuestionRequest;
import com.jnvstguru.jnvst_guru_backend.api.dto.ArithmeticQuestionResponse;
import com.jnvstguru.jnvst_guru_backend.api.dto.StudentArithmeticQuestionResponse;
import com.jnvstguru.jnvst_guru_backend.domain.ArithmeticQuestionEnums;
import com.jnvstguru.jnvst_guru_backend.service.ArithmeticQuestionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ArithmeticQuestionController {
    private final ArithmeticQuestionService arithmeticQuestionService;

    public ArithmeticQuestionController(ArithmeticQuestionService arithmeticQuestionService) {
        this.arithmeticQuestionService = arithmeticQuestionService;
    }

    @GetMapping("/student/arithmetic-questions")
    public ResponseEntity<Page<StudentArithmeticQuestionResponse>> getStudentQuestions(
            @RequestParam(required = false) ArithmeticQuestionEnums.Language language,
            @RequestParam(required = false) ArithmeticQuestionEnums.QuestionType questionType,
            @RequestParam(required = false) ArithmeticQuestionEnums.Difficulty difficulty,
            @PageableDefault(size = 20, sort = "questionId") Pageable pageable) {
        return ResponseEntity.ok(arithmeticQuestionService.getStudentQuestions(language, questionType, difficulty, pageable));
    }

    @GetMapping("/arithmetic-questions")
    public ResponseEntity<Page<ArithmeticQuestionResponse>> getQuestions(
            @RequestParam(required = false) ArithmeticQuestionEnums.QuestionType questionType,
            @RequestParam(required = false) ArithmeticQuestionEnums.Difficulty difficulty,
            @RequestParam(required = false) ArithmeticQuestionEnums.Status status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(arithmeticQuestionService.getQuestions(questionType, difficulty, status, pageable));
    }

    @GetMapping("/arithmetic-questions/{id}")
    public ResponseEntity<ArithmeticQuestionResponse> getQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(arithmeticQuestionService.getQuestion(id));
    }

    @PostMapping("/arithmetic-questions")
    public ResponseEntity<ArithmeticQuestionResponse> createQuestion(@Valid @RequestBody ArithmeticQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(arithmeticQuestionService.createQuestion(request));
    }

    @PutMapping("/arithmetic-questions/{id}")
    public ResponseEntity<ArithmeticQuestionResponse> updateQuestion(@PathVariable Long id,
                                                                  @Valid @RequestBody ArithmeticQuestionRequest request) {
        return ResponseEntity.ok(arithmeticQuestionService.updateQuestion(id, request));
    }

    @DeleteMapping("/arithmetic-questions/{id}")
    public ResponseEntity<ArithmeticQuestionResponse> deleteQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(arithmeticQuestionService.deleteQuestion(id));
    }
}
