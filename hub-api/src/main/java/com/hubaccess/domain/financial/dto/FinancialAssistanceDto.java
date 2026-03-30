package com.hubaccess.domain.financial.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FinancialAssistanceDto(
        UUID id,
        String faType,
        String status,
        LocalDate effectiveDate,
        LocalDate expirationDate,
        BigDecimal copayMaxBenefitUsd,
        BigDecimal copayUsedYtdUsd,
        BigDecimal papFplPercentage,
        String denialReason
) {}
