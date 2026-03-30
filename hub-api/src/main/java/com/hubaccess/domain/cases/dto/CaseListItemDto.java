package com.hubaccess.domain.cases.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CaseListItemDto(
        UUID id,
        String caseNumber,
        String patientName,
        String programName,
        String stage,
        String workflowState,
        String insuranceType,
        Boolean slaBreachFlag,
        Boolean escalationFlag,
        String assignedCmName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
