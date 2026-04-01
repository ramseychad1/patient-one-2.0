package com.hubaccess.domain.childcase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PapCaseDto(
        UUID id,
        UUID hubCaseId,
        String papCaseNumber,
        String status,
        String reason,
        BigDecimal fplPercentage,
        Integer householdSize,
        BigDecimal annualIncomeUsd,
        String incomeVerifiedMethod,
        Boolean hardshipWaiverApplied,
        LocalDate approvalEffectiveDate,
        LocalDate approvalExpiryDate,
        String denialReason,
        String ineligibleReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
