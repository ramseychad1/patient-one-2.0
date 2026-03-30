package com.hubaccess.domain.activity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InteractionDto(
        UUID id,
        String interactionType,
        String direction,
        String channel,
        String contactName,
        String contactRole,
        String subject,
        String body,
        Integer durationMinutes,
        Boolean adverseEventReported,
        OffsetDateTime createdAt
) {}
