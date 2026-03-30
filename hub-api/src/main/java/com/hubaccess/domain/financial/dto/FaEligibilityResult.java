package com.hubaccess.domain.financial.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FaEligibilityResult(
        String faType,
        String status,
        String reason,
        String reasonCode,
        BigDecimal incomeFplPct,
        Integer fplThresholdPct
) {
    public static FaEligibilityResult eligible(String faType) {
        return new FaEligibilityResult(faType, "ELIGIBLE", null, null, null, null);
    }

    public static FaEligibilityResult ineligible(String faType, String reason, String reasonCode) {
        return new FaEligibilityResult(faType, "INELIGIBLE", reason, reasonCode, null, null);
    }

    public static FaEligibilityResult pendingVerification(String faType, String reason) {
        return new FaEligibilityResult(faType, "PENDING_INCOME_VERIFICATION", reason, null, null, null);
    }

    public static FaEligibilityResult ineligibleWithFpl(String faType, String reason, String reasonCode,
                                                         BigDecimal fplPct, Integer threshold) {
        return new FaEligibilityResult(faType, "INELIGIBLE", reason, reasonCode, fplPct, threshold);
    }
}
