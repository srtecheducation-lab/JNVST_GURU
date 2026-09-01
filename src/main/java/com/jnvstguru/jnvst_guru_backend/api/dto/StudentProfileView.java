package com.jnvstguru.jnvst_guru_backend.api.dto;

import java.time.OffsetDateTime;

public record StudentProfileView(
        Long id,
        String name,
        String classLevel,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
