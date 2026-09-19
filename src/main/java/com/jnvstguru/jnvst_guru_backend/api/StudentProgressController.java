package com.jnvstguru.jnvst_guru_backend.api;

import com.jnvstguru.jnvst_guru_backend.api.dto.StudentProgressResponse;
import com.jnvstguru.jnvst_guru_backend.service.StudentProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/progress")
public class StudentProgressController {
    private final StudentProgressService service;

    public StudentProgressController(StudentProgressService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<StudentProgressResponse> getProgress(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int recentPage,
            @RequestParam(defaultValue = "20") int recentLimit) {
        if (!(authentication instanceof JwtAuthenticationToken token)) {
            throw new IllegalStateException("Authenticated JWT is required.");
        }
        return ResponseEntity.ok(service.getProgress(
                UUID.fromString(token.getToken().getSubject()), recentPage, recentLimit));
    }
}
