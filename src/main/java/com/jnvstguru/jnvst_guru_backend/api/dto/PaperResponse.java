package com.jnvstguru.jnvst_guru_backend.api.dto;

import com.jnvstguru.jnvst_guru_backend.domain.PaperStatus;

import java.time.OffsetDateTime;

public record PaperResponse(
        Long id,
        String code,
        String name,
        Integer examYear,
        PaperStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
