package com.hubaccess.domain.childcase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CopayCaseDto(
        UUID id,
        UUID hubCaseId,
        String copayCaseNumber,
        String status,
        String reason,
        Boolean aksCheckPassed,
        String aksBlockReason,
        String cardNumber,
        String bin,
        String pcn,
        String groupCode,
        BigDecimal maxBenefitUsd,
        BigDecimal usedYtdUsd,
        LocalDate effectiveDate,
        LocalDate expirationDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
