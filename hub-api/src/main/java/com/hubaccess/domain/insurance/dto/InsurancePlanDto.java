package com.hubaccess.domain.insurance.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InsurancePlanDto(
        UUID id,
        String insuranceType,
        String planName,
        String payerName,
        String memberId,
        String groupNumber,
        String bin,
        String pcn,
        String planType,
        BigDecimal deductibleIndividual,
        BigDecimal deductibleMet,
        BigDecimal oopMaxIndividual,
        BigDecimal oopMet,
        BigDecimal copayDrug,
        BigDecimal coinsurancePct,
        Integer formularyTier,
        Boolean paRequired,
        Boolean stepTherapyRequired,
        String[] coveredSpecialtyPharmacies,
        Boolean isPrimary
) {}
