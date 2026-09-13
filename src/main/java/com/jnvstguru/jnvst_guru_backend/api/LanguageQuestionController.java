package com.jnvstguru.jnvst_guru_backend.api;

import com.jnvstguru.jnvst_guru_backend.api.dto.StudentLanguagePassageResponse;
import com.jnvstguru.jnvst_guru_backend.domain.LanguageCode;
import com.jnvstguru.jnvst_guru_backend.service.LanguageQuestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/student/language-questions")
public class LanguageQuestionController {
    private final LanguageQuestionService service;

    public LanguageQuestionController(LanguageQuestionService service) {
        this.service = service;
    }

    @GetMapping
    public Page<StudentLanguagePassageResponse> getQuestions(
            @RequestParam LanguageCode language,
            Pageable pageable) {
        return service.getStudentPassages(language, pageable);
    }
}
