package com.hubaccess.domain.childcase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AeCaseDto(
        UUID id,
        UUID hubCaseId,
        String aeCaseNumber,
        String aeType,
        String status,
        String submissionStatus,
        String reportedBy,
        String eventDescription,
        LocalDate eventDate,
        OffsetDateTime manufacturerNotifiedAt,
        OffsetDateTime sla1bdDeadline,
        OffsetDateTime sla3cdDeadline,
        Boolean slaBreached,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
