package com.jnvstguru.jnvst_guru_backend.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentProfileRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @Size(max = 50, message = "Class level must be at most 50 characters")
        String classLevel
) {
}
