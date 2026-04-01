package com.hubaccess.domain.childcase;

import com.hubaccess.domain.childcase.dto.*;
import com.hubaccess.web.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cases/{caseId}")
@RequiredArgsConstructor
public class ChildCaseController {

    private final BiCaseRepository biCaseRepo;
    private final PapCaseRepository papCaseRepo;
    private final CopayCaseRepository copayCaseRepo;
    private final AeCaseRepository aeCaseRepo;
    private final MissingInformationRepository miRepo;

    @GetMapping("/bi-cases")
    public ResponseEntity<ApiResponse<List<BiCaseDto>>> getBiCases(
            @PathVariable UUID caseId, Authentication auth) {
        List<BiCaseDto> result = biCaseRepo.findByHubCaseId(caseId).stream()
                .map(e -> new BiCaseDto(
                        e.getId(), e.getHubCaseId(), e.getBiCaseNumber(), e.getBiType(),
                        e.getStatus(), e.getReason(),
                        e.getObcAttempt1At(), e.getObcAttempt1Result(),
                        e.getObcAttempt2At(), e.getObcAttempt2Result(),
                        e.getObcAttempt3At(), e.getObcAttempt3Result(),
                        e.getSmsSentAt(), e.getFaxToHcpSentAt(),
                        e.getCoverageOutcome(), e.getPlanType(),
                        e.getFormularyTier(), e.getPaRequired(),
                        e.getCreatedAt(), e.getUpdatedAt()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/pap-cases")
    public ResponseEntity<ApiResponse<List<PapCaseDto>>> getPapCases(
            @PathVariable UUID caseId, Authentication auth) {
        List<PapCaseDto> result = papCaseRepo.findByHubCaseId(caseId).stream()
                .map(e -> new PapCaseDto(
                        e.getId(), e.getHubCaseId(), e.getPapCaseNumber(),
                        e.getStatus(), e.getReason(),
                        e.getFplPercentage(), e.getHouseholdSize(), e.getAnnualIncomeUsd(),
                        e.getIncomeVerifiedMethod(), e.getHardshipWaiverApplied(),
                        e.getApprovalEffectiveDate(), e.getApprovalExpiryDate(),
                        e.getDenialReason(), e.getIneligibleReason(),
                        e.getCreatedAt(), e.getUpdatedAt()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/copay-cases")
    public ResponseEntity<ApiResponse<List<CopayCaseDto>>> getCopayCases(
            @PathVariable UUID caseId, Authentication auth) {
        List<CopayCaseDto> result = copayCaseRepo.findByHubCaseId(caseId).stream()
                .map(e -> new CopayCaseDto(
                        e.getId(), e.getHubCaseId(), e.getCopayCaseNumber(),
                        e.getStatus(), e.getReason(),
                        e.getAksCheckPassed(), e.getAksBlockReason(),
                        e.getCardNumber(), e.getBin(), e.getPcn(), e.getGroupCode(),
                        e.getMaxBenefitUsd(), e.getUsedYtdUsd(),
                        e.getEffectiveDate(), e.getExpirationDate(),
                        e.getCreatedAt(), e.getUpdatedAt()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/ae-cases")
    public ResponseEntity<ApiResponse<List<AeCaseDto>>> getAeCases(
            @PathVariable UUID caseId, Authentication auth) {
        List<AeCaseDto> result = aeCaseRepo.findByHubCaseId(caseId).stream()
                .map(e -> new AeCaseDto(
                        e.getId(), e.getHubCaseId(), e.getAeCaseNumber(),
                        e.getAeType(), e.getStatus(), e.getSubmissionStatus(),
                        e.getReportedBy(), e.getEventDescription(), e.getEventDate(),
                        e.getManufacturerNotifiedAt(),
                        e.getSla1bdDeadline(), e.getSla3cdDeadline(), e.getSlaBreached(),
                        e.getCreatedAt(), e.getUpdatedAt()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/missing-info")
    public ResponseEntity<ApiResponse<List<MissingInformationDto>>> getMissingInfo(
            @PathVariable UUID caseId, Authentication auth) {
        List<MissingInformationDto> result = miRepo.findByHubCaseId(caseId).stream()
                .map(e -> new MissingInformationDto(
                        e.getId(), e.getHubCaseId(), e.getMiNumber(),
                        e.getCategory(), e.getDetail(), e.getMiType(),
                        e.getReportedDate(), e.getReceivedDate(),
                        e.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
