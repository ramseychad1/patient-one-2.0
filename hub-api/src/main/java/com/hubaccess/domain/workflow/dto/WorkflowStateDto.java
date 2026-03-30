package com.hubaccess.domain.workflow.dto;

import java.time.OffsetDateTime;

public record WorkflowStateDto(
        String state,
        String nextRequiredAction,
        String nextActionLabel,
        OffsetDateTime nextActionDeadline,
        OffsetDateTime enteredAt
) {}
