package com.hubaccess.domain.financial;

import com.hubaccess.domain.cases.HubCase;
import com.hubaccess.domain.financial.dto.FaEligibilityResult;
import com.hubaccess.domain.insurance.InsurancePlan;
import com.hubaccess.domain.patient.Patient;
import com.hubaccess.domain.program.ProgramConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class FaEligibilityEngine {

    private static final Set<String> GOVERNMENT_TYPES = Set.of(
            "MEDICARE", "MEDICAID", "TRICARE", "VA", "MEDICARE_ADVANTAGE"
    );

    // 2025 Federal Poverty Levels (48 contiguous states)
    private static final int[] FPL_2025 = {
            0,      // 0 - unused
            15_060, // 1 person
            20_440, // 2
            25_820, // 3
            31_200, // 4
            36_580, // 5
            41_960, // 6
            47_340, // 7
            52_720  // 8
    };

    @Value("${demo.scenario:DEFAULT}")
    private String demoScenario;

    private String getEffectiveScenario() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String header = attrs.getRequest().getHeader("X-Demo-Scenario");
                if (header != null && !header.isBlank()) return header;
            }
        } catch (Exception ignored) {}
        return demoScenario;
    }

    public List<FaEligibilityResult> evaluate(HubCase hc, InsurancePlan ip, ProgramConfig config, Patient patient) {
        List<FaEligibilityResult> results = new ArrayList<>();
        String effectiveScenario = getEffectiveScenario();

        // DEMO_SCENARIO overrides
        if ("GOVERNMENT_PATIENT".equals(effectiveScenario)) {
            results.add(FaEligibilityResult.ineligible("COPAY",
                    "Government-insured patients are ineligible for copay assistance (Anti-Kickback Statute)",
                    "AKS_RULE_VIOLATION"));
            log.warn("AKS RULE TRIGGERED: Copay blocked for government-insured patient, case={}", hc.getCaseNumber());
            results.add(FaEligibilityResult.eligible("PAP"));
            if (Boolean.TRUE.equals(config.getBridgeSupplyEnabled())) {
                results.add(FaEligibilityResult.eligible("BRIDGE"));
            }
            return results;
        }

        if ("UNINSURED_PATIENT".equals(effectiveScenario)) {
            results.add(FaEligibilityResult.ineligible("COPAY", "No insurance — copay not applicable", "NO_INSURANCE"));
            results.add(FaEligibilityResult.pendingVerification("PAP", "Income verification required"));
            return results;
        }

        if ("EIV_INELIGIBLE".equals(effectiveScenario)) {
            results.add(FaEligibilityResult.eligible("COPAY"));
            results.add(FaEligibilityResult.ineligibleWithFpl("PAP",
                    "Income exceeds FPL threshold",
                    "INCOME_EXCEEDS_THRESHOLD",
                    new BigDecimal("520.00"),
                    config.getFplThresholdPct()));
            return results;
        }

        // ── Copay eligibility ────────────────────────────────────
        results.add(evaluateCopay(hc, ip, config));

        // ── PAP eligibility ──────────────────────────────────────
        if (Boolean.TRUE.equals(config.getPapEnabled())) {
            results.add(evaluatePap(hc, ip, config, patient));
        }

        // ── Bridge eligibility ───────────────────────────────────
        if (Boolean.TRUE.equals(config.getBridgeSupplyEnabled())) {
            results.add(evaluateBridge(hc, config));
        }

        return results;
    }

    private FaEligibilityResult evaluateCopay(HubCase hc, InsurancePlan ip, ProgramConfig config) {
        if (!Boolean.TRUE.equals(config.getCopayAssistanceEnabled())) {
            return FaEligibilityResult.ineligible("COPAY", "Copay assistance not enabled for this program", "NOT_CONFIGURED");
        }

        if (ip != null && GOVERNMENT_TYPES.contains(ip.getInsuranceType())) {
            log.warn("AKS RULE TRIGGERED: Copay blocked for government-insured patient, case={}", hc.getCaseNumber());
            return FaEligibilityResult.ineligible("COPAY",
                    "Government-insured patients are ineligible for copay assistance (Anti-Kickback Statute)",
                    "AKS_RULE_VIOLATION");
        }

        if ("GOVERNMENT".equals(hc.getInsuranceType())) {
            log.warn("AKS RULE TRIGGERED: Copay blocked for government-insured patient, case={}", hc.getCaseNumber());
            return FaEligibilityResult.ineligible("COPAY",
                    "Government-insured patients are ineligible for copay assistance (Anti-Kickback Statute)",
                    "AKS_RULE_VIOLATION");
        }

        if ("UNINSURED".equals(hc.getInsuranceType())) {
            return FaEligibilityResult.ineligible("COPAY", "No insurance — copay not applicable", "NO_INSURANCE");
        }

        return FaEligibilityResult.eligible("COPAY");
    }

    private FaEligibilityResult evaluatePap(HubCase hc, InsurancePlan ip, ProgramConfig config, Patient patient) {
        if (patient == null || patient.getAnnualIncomeUsd() == null || patient.getHouseholdSize() == null) {
            return FaEligibilityResult.pendingVerification("PAP", "Income verification required for PAP assessment");
        }

        BigDecimal fplPct = calculateFplPercentage(patient.getAnnualIncomeUsd(), patient.getHouseholdSize());
        int threshold = config.getFplThresholdPct() != null ? config.getFplThresholdPct() : 400;

        if (fplPct.compareTo(new BigDecimal(threshold)) > 0) {
            return FaEligibilityResult.ineligibleWithFpl("PAP",
                    "Income exceeds FPL threshold (" + fplPct.intValue() + "% vs " + threshold + "% limit)",
                    "INCOME_EXCEEDS_THRESHOLD", fplPct, threshold);
        }

        return FaEligibilityResult.eligible("PAP");
    }

    private FaEligibilityResult evaluateBridge(HubCase hc, ProgramConfig config) {
        if (Boolean.TRUE.equals(hc.getPaRequired()) &&
                List.of("PA_PENDING", "PA_SUBMITTED").contains(hc.getCurrentWorkflowState())) {
            return FaEligibilityResult.eligible("BRIDGE");
        }
        return FaEligibilityResult.ineligible("BRIDGE", "No active trigger condition (PA not pending)", "NO_TRIGGER");
    }

    private BigDecimal calculateFplPercentage(BigDecimal income, int householdSize) {
        int idx = Math.min(householdSize, FPL_2025.length - 1);
        if (idx < 1) idx = 1;
        BigDecimal fplDollar = new BigDecimal(FPL_2025[idx]);
        return income.divide(fplDollar, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
    }
}
