package com.jnvstguru.jnvst_guru_backend.api;

import com.jnvstguru.jnvst_guru_backend.api.dto.MeResponse;
import com.jnvstguru.jnvst_guru_backend.api.dto.StudentProfileView;
import com.jnvstguru.jnvst_guru_backend.domain.StudentProfileEntity;
import com.jnvstguru.jnvst_guru_backend.domain.UserEntity;
import com.jnvstguru.jnvst_guru_backend.service.ApplicationUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class MeController {
    private static final Logger log = LoggerFactory.getLogger(MeController.class);

    private final ApplicationUserService applicationUserService;

    public MeController(ApplicationUserService applicationUserService) {
        this.applicationUserService = applicationUserService;
    }

    @GetMapping(value = {"/me", "/me/"})
    public ResponseEntity<MeResponse> getCurrentUser(Authentication authentication, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String safeAuthHeader = authHeader == null ? "null" : authHeader.length() > 40
                ? authHeader.substring(0, 40) + "..."
                : authHeader;

        log.info("[ME] Incoming request: method={} uri={} queryString={} authType={} principal={} authorization={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                authentication == null ? "null" : authentication.getClass().getName(),
                authentication == null ? "null" : authentication.getName(),
                safeAuthHeader);

        if (authentication == null) {
            log.warn("[ME] Missing authentication for request uri={}", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!(authentication instanceof JwtAuthenticationToken token)) {
            log.warn("[ME] Unexpected authentication type={} for request uri={}",
                    authentication.getClass().getName(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Jwt jwt = token.getToken();
        UUID authUserId = UUID.fromString(jwt.getSubject());
        log.info("[ME] Fetching current user for authUserId={}", authUserId);

        UserEntity user = applicationUserService.findByAuthUserId(authUserId);
        StudentProfileEntity profile = applicationUserService.getStudentProfile(user);

        StudentProfileView profileView = profile == null ? null
                : new StudentProfileView(
                        profile.getId(),
                        profile.getName(),
                        profile.getClassLevel(),
                        profile.getCreatedAt(),
                        profile.getUpdatedAt());

        MeResponse response = new MeResponse(
                user.getId(),
                user.getAuthUserId(),
                user.getStatus(),
                applicationUserService.getRoleCodesForUser(authUserId),
                profileView,
                user.getCreatedAt(),
                user.getUpdatedAt());

        log.info("[ME] Returning user summary for userId={}", user.getId());
        return ResponseEntity.ok(response);
    }
}
