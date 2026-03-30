package com.hubaccess.domain.activity.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank String title,
        String description,
        String priority,
        OffsetDateTime dueDate,
        UUID assignedToId
) {}
