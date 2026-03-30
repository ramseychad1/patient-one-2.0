package com.hubaccess.domain.outreach.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PatientOutreachDto(
        UUID id,
        String outreachType,
        String channel,
        String recipientType,
        String deliveryStatus,
        String stubMessageId,
        String messageBody,
        String uniqueUrl,
        String accessCode,
        OffsetDateTime createdAt,
        OffsetDateTime resolvedAt
) {}
