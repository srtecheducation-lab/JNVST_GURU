package com.jnvstguru.jnvst_guru_backend.api;

import com.jnvstguru.jnvst_guru_backend.api.dto.StudentProfileRequest;
import com.jnvstguru.jnvst_guru_backend.domain.StudentProfileEntity;
import com.jnvstguru.jnvst_guru_backend.domain.UserEntity;
import com.jnvstguru.jnvst_guru_backend.service.ApplicationUserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
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

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("exists", true);
        response.put("id", profile.getId());
        response.put("userId", profile.getUser().getId());
        response.put("name", profile.getName());
        response.put("dateOfBirth", profile.getDateOfBirth());
        response.put("gender", profile.getGender());
        response.put("category", profile.getCategory());
        response.put("residentialArea", profile.getResidentialArea());
        response.put("classLevel", profile.getClassLevel());
        if (profile.getState() != null) {
            response.put("stateId", profile.getState().getId());
            response.put("stateName", profile.getState().getName());
        }
        if (profile.getDistrict() != null) {
            response.put("districtId", profile.getDistrict().getId());
            response.put("districtName", profile.getDistrict().getName());
        }
        response.put("preferredLanguage", profile.getPreferredLanguage());
        if (profile.getExamSession() != null) {
            response.put("examSessionId", profile.getExamSession().getId());
        }

        Map<String, Object> examSession = new LinkedHashMap<>();
        if (profile.getExamSession() != null) {
            examSession.put("id", profile.getExamSession().getId());
            examSession.put("sessionName", profile.getExamSession().getSessionName());
        }
        response.put("examSession", examSession.isEmpty() ? null : examSession);

        response.put("createdAt", profile.getCreatedAt());
        response.put("updatedAt", profile.getUpdatedAt());
        return ResponseEntity.ok(response);
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

        StudentProfileEntity profile = applicationUserService.createStudentProfile(user, request);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", profile.getId());
        response.put("userId", profile.getUser().getId());
        response.put("name", profile.getName());
        response.put("dateOfBirth", profile.getDateOfBirth());
        response.put("gender", profile.getGender());
        response.put("category", profile.getCategory());
        response.put("residentialArea", profile.getResidentialArea());
        response.put("classLevel", profile.getClassLevel());
        if (profile.getState() != null) {
            response.put("stateId", profile.getState().getId());
            response.put("stateName", profile.getState().getName());
        }
        if (profile.getDistrict() != null) {
            response.put("districtId", profile.getDistrict().getId());
            response.put("districtName", profile.getDistrict().getName());
        }
        response.put("preferredLanguage", profile.getPreferredLanguage());
        if (profile.getExamSession() != null) {
            response.put("examSessionId", profile.getExamSession().getId());
        }

        Map<String, Object> examSession = new LinkedHashMap<>();
        if (profile.getExamSession() != null) {
            examSession.put("id", profile.getExamSession().getId());
            examSession.put("sessionName", profile.getExamSession().getSessionName());
        }
        response.put("examSession", examSession.isEmpty() ? null : examSession);

        response.put("createdAt", profile.getCreatedAt());
        response.put("updatedAt", profile.getUpdatedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
