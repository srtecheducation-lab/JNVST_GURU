package com.jnvstguru.jnvst_guru_backend.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MeResponse(
        Long userId,
        UUID authUserId,
        String status,
        List<String> roles,
        StudentProfileView studentProfile,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
