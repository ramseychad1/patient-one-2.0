package com.hubaccess.domain.activity.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record TimelineEntryDto(
        UUID id,
        String entryType,
        String title,
        String description,
        String performedBy,
        OffsetDateTime occurredAt,
        Map<String, String> metadata
) {}
