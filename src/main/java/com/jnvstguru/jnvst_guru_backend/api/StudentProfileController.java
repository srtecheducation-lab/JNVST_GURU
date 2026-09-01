package com.jnvstguru.jnvst_guru_backend.api;

import com.jnvstguru.jnvst_guru_backend.api.dto.StudentProfileRequest;
import com.jnvstguru.jnvst_guru_backend.domain.StudentProfileEntity;
import com.jnvstguru.jnvst_guru_backend.domain.UserEntity;
import com.jnvstguru.jnvst_guru_backend.service.ApplicationUserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class StudentProfileController {
    private static final Logger log = LoggerFactory.getLogger(StudentProfileController.class);

    private final ApplicationUserService applicationUserService;

    public StudentProfileController(ApplicationUserService applicationUserService) {
        this.applicationUserService = applicationUserService;
    }

    @GetMapping("/student-profiles/me")
    public ResponseEntity<Map<String, Object>> getStudentProfile(Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        Jwt jwt = token.getToken();
        UUID authUserId = UUID.fromString(jwt.getSubject());
        UserEntity user = applicationUserService.findByAuthUserId(authUserId);
        log.info("[STUDENT_PROFILE] Fetching profile for userId={}", user.getId());
        StudentProfileEntity profile = applicationUserService.getStudentProfile(user);

        if (profile == null) {
            log.info("[STUDENT_PROFILE] Profile not found for userId={}", user.getId());
            return ResponseEntity.ok(Map.of("exists", false, "userId", user.getId()));
        }

        return ResponseEntity.ok(Map.of(
                "exists", true,
                "id", profile.getId(),
                "userId", profile.getUser().getId(),
                "name", profile.getName(),
                "classLevel", profile.getClassLevel(),
                "createdAt", profile.getCreatedAt(),
                "updatedAt", profile.getUpdatedAt()));
    }

    @PostMapping("/student-profiles")
    public ResponseEntity<Map<String, Object>> createStudentProfile(
            Authentication authentication,
            @Valid @RequestBody StudentProfileRequest request) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        Jwt jwt = token.getToken();
        UUID authUserId = UUID.fromString(jwt.getSubject());
        UserEntity user = applicationUserService.findByAuthUserId(authUserId);
        log.info("[STUDENT_PROFILE] Creating profile for userId={}", user.getId());

        StudentProfileEntity profile = applicationUserService.createStudentProfile(user, request.name(), request.classLevel());

        return ResponseEntity.ok(Map.of(
                "id", profile.getId(),
                "userId", profile.getUser().getId(),
                "name", profile.getName(),
                "classLevel", profile.getClassLevel(),
                "createdAt", profile.getCreatedAt(),
                "updatedAt", profile.getUpdatedAt()));
    }
}
