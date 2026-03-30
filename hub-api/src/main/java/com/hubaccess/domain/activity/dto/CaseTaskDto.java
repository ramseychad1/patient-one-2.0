package com.hubaccess.domain.activity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CaseTaskDto(
        UUID id,
        UUID caseId,
        String taskType,
        String title,
        String description,
        String status,
        String priority,
        String actionKey,
        Boolean slaBreached,
        OffsetDateTime dueAt,
        OffsetDateTime createdAt
) {}
