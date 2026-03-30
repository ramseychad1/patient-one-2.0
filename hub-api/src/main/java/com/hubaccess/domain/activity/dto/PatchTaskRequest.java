package com.hubaccess.domain.activity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PatchTaskRequest(
        String title,
        String description,
        String priority,
        OffsetDateTime dueDate,
        UUID assignedToId,
        OffsetDateTime completedAt,
        String completionNotes
) {}
