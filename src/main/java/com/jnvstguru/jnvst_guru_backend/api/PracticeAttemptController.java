package com.jnvstguru.jnvst_guru_backend.api;

import com.jnvstguru.jnvst_guru_backend.api.dto.*;
import com.jnvstguru.jnvst_guru_backend.domain.*;
import com.jnvstguru.jnvst_guru_backend.service.PracticeAttemptService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/practice-attempts")
public class PracticeAttemptController {
    private final PracticeAttemptService service;

    public PracticeAttemptController(PracticeAttemptService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PracticeAttemptResponse> submit(
            Authentication authentication, @RequestBody PracticeAttemptRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(authUserId(authentication), request));
    }

    @GetMapping
    public ResponseEntity<PracticeStatusResponse> status(
            Authentication authentication,
            @RequestParam PracticeMode practiceMode,
            @RequestParam PracticeSubject subject,
            @RequestParam(required = false) ArithmeticQuestionEnums.QuestionType topic,
            @RequestParam ArithmeticQuestionEnums.Difficulty difficulty) {
        return ResponseEntity.ok(service.getStatus(authUserId(authentication), practiceMode, subject, topic, difficulty));
    }

    @GetMapping("/latest")
    public ResponseEntity<PracticeAttemptResponse> latest(
            Authentication authentication,
            @RequestParam PracticeMode practiceMode,
            @RequestParam PracticeSubject subject,
            @RequestParam(required = false) ArithmeticQuestionEnums.QuestionType topic,
            @RequestParam ArithmeticQuestionEnums.Difficulty difficulty,
            @RequestParam Integer page) {
        return ResponseEntity.ok(service.getLatest(authUserId(authentication), practiceMode, subject, topic, difficulty, page));
    }

    private UUID authUserId(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken token)) {
            throw new IllegalStateException("Authenticated JWT is required.");
        }
        try {
            return UUID.fromString(token.getToken().getSubject());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Authenticated JWT subject is invalid.", ex);
        }
    }
}
