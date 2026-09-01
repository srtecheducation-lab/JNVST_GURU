package com.jnvstguru.jnvst_guru_backend.api.dto;

import java.time.OffsetDateTime;

public record SubscriptionResponse(
        Long id,
        Long userId,
        Long planId,
        String planCode,
        String planName,
        String status,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
