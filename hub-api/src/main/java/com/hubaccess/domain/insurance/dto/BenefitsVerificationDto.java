package com.hubaccess.domain.insurance.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BenefitsVerificationDto(
        UUID id,
        String verificationType,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime completedAt
) {}
