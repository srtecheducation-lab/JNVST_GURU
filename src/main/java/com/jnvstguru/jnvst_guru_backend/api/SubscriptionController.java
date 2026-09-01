package com.jnvstguru.jnvst_guru_backend.api;

import com.jnvstguru.jnvst_guru_backend.api.dto.SubscriptionResponse;
import com.jnvstguru.jnvst_guru_backend.domain.SubscriptionEntity;
import com.jnvstguru.jnvst_guru_backend.domain.UserEntity;
import com.jnvstguru.jnvst_guru_backend.service.ApplicationUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class SubscriptionController {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);

    private final ApplicationUserService applicationUserService;

    public SubscriptionController(ApplicationUserService applicationUserService) {
        this.applicationUserService = applicationUserService;
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptions(Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        Jwt jwt = token.getToken();
        UUID authUserId = UUID.fromString(jwt.getSubject());
        UserEntity user = applicationUserService.findByAuthUserId(authUserId);
        log.info("[SUBSCRIPTION] Fetching subscriptions for userId={}", user.getId());

        List<SubscriptionEntity> subscriptions = applicationUserService.getSubscriptions(user);
        List<SubscriptionResponse> payload = subscriptions.stream()
                .map(subscription -> new SubscriptionResponse(
                        subscription.getId(),
                        subscription.getUser().getId(),
                        subscription.getPlan().getId(),
                        subscription.getPlan().getCode(),
                        subscription.getPlan().getName(),
                        subscription.getStatus(),
                        subscription.getStartAt(),
                        subscription.getEndAt(),
                        subscription.getCreatedAt(),
                        subscription.getUpdatedAt()))
                .toList();

        log.info("[SUBSCRIPTION] Returning {} subscription(s) for userId={}", payload.size(), user.getId());
        return ResponseEntity.ok(payload);
    }
}
