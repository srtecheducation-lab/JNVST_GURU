package com.jnvstguru.jnvst_guru_backend.api.dto;

import com.jnvstguru.jnvst_guru_backend.domain.StudentProfileEnums;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StudentProfileRequest(
       @NotBlank(message = "Name is required")
       @Size(max = 150, message = "Name must be at most 150 characters")
       String name,

       @NotNull(message = "Date of birth is required")
       @Past(message = "Date of birth must be in the past")
       LocalDate dateOfBirth,

       @NotNull(message = "Gender is required")
       StudentProfileEnums.Gender gender,

       @NotNull(message = "Category is required")
       StudentProfileEnums.Category category,

       @NotNull(message = "Residential area is required")
       StudentProfileEnums.ResidentialArea residentialArea,

       @NotNull(message = "Class level is required")
       @Min(value = 6, message = "Class level must be at least 6")
       Integer classLevel,

       @NotNull(message = "State is required")
       Long stateId,

       @NotNull(message = "District is required")
       Long districtId,

       @NotBlank(message = "Preferred language is required")
       @Size(max = 20, message = "Preferred language must be at most 20 characters")
       String preferredLanguage,

       @NotNull(message = "Exam session is required")
       Long examSessionId
) {
}
