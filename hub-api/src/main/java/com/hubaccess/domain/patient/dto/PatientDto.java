package com.hubaccess.domain.patient.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PatientDto(
        UUID id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String zip,
        String phonePrimary,
        String phoneSecondary,
        String email,
        String preferredContactMethod,
        String preferredLanguage
) {}
