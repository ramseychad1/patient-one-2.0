package com.hubaccess.domain.pa.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PriorAuthorizationDto(
        UUID id,
        Integer attemptNumber,
        String paType,
        String status,
        String payerName,
        String submissionMethod,
        OffsetDateTime submittedAt,
        OffsetDateTime determinedAt,
        String authorizationNumber,
        String denialReason,
        OffsetDateTime appealDeadline
) {}
