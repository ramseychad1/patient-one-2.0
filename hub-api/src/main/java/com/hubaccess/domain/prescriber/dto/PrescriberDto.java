package com.hubaccess.domain.prescriber.dto;

import java.util.UUID;

public record PrescriberDto(
        UUID id,
        String npi,
        String firstName,
        String lastName,
        String practiceName,
        String specialty,
        String phone,
        String fax
) {}
