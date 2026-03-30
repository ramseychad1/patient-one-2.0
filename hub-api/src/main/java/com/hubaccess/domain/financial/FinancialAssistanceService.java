package com.hubaccess.domain.financial;

import com.hubaccess.domain.cases.HubCase;
import com.hubaccess.domain.cases.HubCaseRepository;
import com.hubaccess.domain.financial.dto.FaEligibilityResult;
import com.hubaccess.domain.financial.dto.FinancialAssistanceDto;
import com.hubaccess.domain.insurance.InsurancePlan;
import com.hubaccess.domain.insurance.InsurancePlanRepository;
import com.hubaccess.domain.patient.Patient;
import com.hubaccess.domain.patient.PatientRepository;
import com.hubaccess.domain.program.ProgramConfig;
import com.hubaccess.domain.program.ProgramConfigRepository;
import com.hubaccess.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialAssistanceService {

    private final FinancialAssistanceCaseRepository faRepo;
    private final HubCaseRepository caseRepo;
    private final InsurancePlanRepository insurancePlanRepo;
    private final ProgramConfigRepository configRepo;
    private final PatientRepository patientRepo;
    private final FaEligibilityEngine eligibilityEngine;

    @Transactional(readOnly = true)
    public List<FinancialAssistanceDto> listFa(UUID caseId) {
        return faRepo.findByCaseId(caseId).stream().map(this::toDto).toList();
    }

    @Transactional
    public List<FaEligibilityResult> evaluate(UUID caseId, AuthenticatedUser user) {
        HubCase hc = caseRepo.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));

        InsurancePlan ip = insurancePlanRepo.findByCaseId(caseId).stream()
                .filter(InsurancePlan::getIsPrimary).findFirst().orElse(null);

        ProgramConfig config = configRepo.findByProgramId(hc.getProgramId())
                .orElseThrow(() -> new EntityNotFoundException("Program config not found"));

        Patient patient = patientRepo.findById(hc.getPatientId()).orElse(null);

        List<FaEligibilityResult> results = eligibilityEngine.evaluate(hc, ip, config, patient);

        // Persist eligibility results as FA records
        for (FaEligibilityResult r : results) {
            if ("ELIGIBLE".equals(r.status())) {
                List<FinancialAssistanceCase> existing = faRepo.findByCaseId(caseId).stream()
                        .filter(fa -> fa.getFaType().equals(r.faType()))
                        .toList();
                if (existing.isEmpty()) {
                    faRepo.save(FinancialAssistanceCase.builder()
                            .caseId(caseId)
                            .faType(r.faType())
                            .status("PENDING")
                            .createdBy(user.id())
                            .build());
                }
            }
        }

        // Update case flags
        results.stream().filter(r -> "COPAY".equals(r.faType())).findFirst().ifPresent(r ->
                hc.setCopayEligible("ELIGIBLE".equals(r.status())));
        results.stream().filter(r -> "PAP".equals(r.faType())).findFirst().ifPresent(r ->
                hc.setPapEligible("ELIGIBLE".equals(r.status())));
        caseRepo.save(hc);

        return results;
    }

    @Transactional
    public FinancialAssistanceDto enrollCopay(UUID caseId, AuthenticatedUser user) {
        HubCase hc = caseRepo.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));

        FinancialAssistanceCase fa = faRepo.findByCaseId(caseId).stream()
                .filter(f -> "COPAY".equals(f.getFaType()))
                .findFirst()
                .orElseGet(() -> faRepo.save(FinancialAssistanceCase.builder()
                        .caseId(caseId).faType("COPAY").status("PENDING").createdBy(user.id()).build()));

        fa.setStatus("ACTIVE");
        fa.setEffectiveDate(LocalDate.now());
        fa.setExpirationDate(LocalDate.now().plusYears(1));
        fa.setCopayCardNumber("CPAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        fa.setCopayBin("610020");
        fa.setCopayPcn("HUBPAY");
        fa.setCopayGroup("MERIDIAN01");
        faRepo.save(fa);
        return toDto(fa);
    }

    @Transactional
    public FinancialAssistanceDto enrollPap(UUID caseId, AuthenticatedUser user) {
        FinancialAssistanceCase fa = faRepo.findByCaseId(caseId).stream()
                .filter(f -> "PAP".equals(f.getFaType()))
                .findFirst()
                .orElseGet(() -> faRepo.save(FinancialAssistanceCase.builder()
                        .caseId(caseId).faType("PAP").status("PENDING").createdBy(user.id()).build()));

        fa.setStatus("APPROVED");
        fa.setEffectiveDate(LocalDate.now());
        fa.setExpirationDate(LocalDate.now().plusMonths(12));
        faRepo.save(fa);
        return toDto(fa);
    }

    private FinancialAssistanceDto toDto(FinancialAssistanceCase fa) {
        return new FinancialAssistanceDto(fa.getId(), fa.getFaType(), fa.getStatus(),
                fa.getEffectiveDate(), fa.getExpirationDate(), fa.getCopayMaxBenefitUsd(),
                fa.getCopayUsedYtdUsd(), fa.getPapFplPercentage(), fa.getDenialReason());
    }
}
