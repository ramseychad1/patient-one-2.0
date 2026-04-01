package com.hubaccess.domain.childcase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BiCaseDto(
        UUID id,
        UUID hubCaseId,
        String biCaseNumber,
        String biType,
        String status,
        String reason,
        OffsetDateTime obcAttempt1At,
        String obcAttempt1Result,
        OffsetDateTime obcAttempt2At,
        String obcAttempt2Result,
        OffsetDateTime obcAttempt3At,
        String obcAttempt3Result,
        OffsetDateTime smsSentAt,
        OffsetDateTime faxToHcpSentAt,
        String coverageOutcome,
        String planType,
        Integer formularyTier,
        Boolean paRequired,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
