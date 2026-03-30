package com.hubaccess.domain.pa;

import com.hubaccess.domain.activity.CaseTask;
import com.hubaccess.domain.activity.CaseTaskRepository;
import com.hubaccess.domain.cases.HubCase;
import com.hubaccess.domain.cases.HubCaseRepository;
import com.hubaccess.domain.pa.dto.CreatePaRequest;
import com.hubaccess.domain.pa.dto.PatchPaRequest;
import com.hubaccess.domain.pa.dto.PriorAuthorizationDto;
import com.hubaccess.domain.program.ProgramConfig;
import com.hubaccess.domain.program.ProgramConfigRepository;
import com.hubaccess.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PriorAuthorizationService {

    private final PriorAuthorizationRepository paRepo;
    private final HubCaseRepository caseRepo;
    private final CaseTaskRepository taskRepo;
    private final ProgramConfigRepository configRepo;

    @Transactional(readOnly = true)
    public List<PriorAuthorizationDto> listPas(UUID caseId) {
        return paRepo.findByCaseIdOrderByAttemptNumberDesc(caseId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public PriorAuthorizationDto createPa(UUID caseId, CreatePaRequest req, AuthenticatedUser user) {
        caseRepo.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));

        int nextAttempt = paRepo.findByCaseIdOrderByAttemptNumberDesc(caseId).stream()
                .mapToInt(PriorAuthorization::getAttemptNumber)
                .max().orElse(0) + 1;

        PriorAuthorization pa = PriorAuthorization.builder()
                .caseId(caseId)
                .attemptNumber(nextAttempt)
                .paType(req.paType())
                .status("PENDING")
                .payerName(req.payerName())
                .payerPaFax(req.payerFax())
                .payerPaPhone(req.payerPhone())
                .performedBy(user.id())
                .build();
        paRepo.save(pa);
        return toDto(pa);
    }

    @Transactional
    public PriorAuthorizationDto patchPa(UUID caseId, UUID paId, PatchPaRequest req, AuthenticatedUser user) {
        PriorAuthorization pa = paRepo.findById(paId)
                .orElseThrow(() -> new EntityNotFoundException("PA not found: " + paId));

        HubCase hc = caseRepo.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));

        if (req.status() != null) {
            validateTransition(pa.getStatus(), req.status());
            pa.setStatus(req.status());

            switch (req.status()) {
                case "SUBMITTED" -> {
                    pa.setSubmittedAt(OffsetDateTime.now());
                    ProgramConfig config = configRepo.findByProgramId(hc.getProgramId()).orElse(null);
                    if (config != null) {
                        pa.setSubmitSlaDeadline(OffsetDateTime.now().plusDays(config.getPaFollowupSlaBusinessDays()));
                    }
                    hc.setPaStatus("SUBMITTED");
                    hc.setCurrentWorkflowState("PA_SUBMITTED");
                }
                case "APPROVED" -> {
                    pa.setDeterminedAt(OffsetDateTime.now());
                    if (req.authorizationNumber() != null) pa.setAuthorizationNumber(req.authorizationNumber());
                    hc.setPaStatus("APPROVED");
                    hc.setCurrentWorkflowState("PA_APPROVED");
                    hc.setCurrentStage("FINANCIAL");
                }
                case "DENIED" -> {
                    pa.setDeterminedAt(OffsetDateTime.now());
                    if (req.denialReason() != null) pa.setDenialReason(req.denialReason());
                    if (req.denialCode() != null) pa.setDenialCode(req.denialCode());
                    hc.setPaStatus("DENIED");
                    hc.setCurrentWorkflowState("PA_DENIED");

                    ProgramConfig config = configRepo.findByProgramId(hc.getProgramId()).orElse(null);
                    int appealDays = config != null ? config.getPaAppealWindowDays() : 30;
                    pa.setAppealDeadline(OffsetDateTime.now().plusDays(appealDays));

                    taskRepo.save(CaseTask.builder()
                            .caseId(caseId)
                            .taskType("INITIATE_APPEAL")
                            .title("Submit PA appeal — denial received")
                            .description("PA denied: " + pa.getDenialReason())
                            .priority("HIGH")
                            .assignedTo(hc.getAssignedCmId())
                            .dueAt(OffsetDateTime.now().plusDays(appealDays))
                            .actionKey("SUBMIT_PA_APPEAL")
                            .build());
                }
                case "APPEAL_DENIED" -> {
                    pa.setDeterminedAt(OffsetDateTime.now());
                    hc.setEscalationFlag(true);
                    hc.setEscalationReason("PA appeal denied — requires supervisor review");
                }
            }
            caseRepo.save(hc);
        }

        paRepo.save(pa);
        return toDto(pa);
    }

    @Transactional
    public PriorAuthorizationDto createAppeal(UUID caseId, UUID deniedPaId, AuthenticatedUser user) {
        PriorAuthorization denied = paRepo.findById(deniedPaId)
                .orElseThrow(() -> new EntityNotFoundException("PA not found: " + deniedPaId));

        int nextAttempt = denied.getAttemptNumber() + 1;
        String paType = nextAttempt == 2 ? "APPEAL_1" : "APPEAL_2";

        PriorAuthorization appeal = PriorAuthorization.builder()
                .caseId(caseId)
                .parentPaId(deniedPaId)
                .attemptNumber(nextAttempt)
                .paType(paType)
                .status("PENDING")
                .payerName(denied.getPayerName())
                .payerPaFax(denied.getPayerPaFax())
                .payerPaPhone(denied.getPayerPaPhone())
                .performedBy(user.id())
                .build();

        HubCase hc = caseRepo.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));
        hc.setPaStatus("APPEALING");
        hc.setCurrentWorkflowState("APPEAL_PENDING");
        caseRepo.save(hc);

        paRepo.save(appeal);
        return toDto(appeal);
    }

    private void validateTransition(String current, String next) {
        boolean valid = switch (current) {
            case "PENDING" -> "SUBMITTED".equals(next);
            case "SUBMITTED" -> List.of("APPROVED", "DENIED").contains(next);
            case "DENIED" -> "APPEAL_SUBMITTED".equals(next);
            case "APPEAL_SUBMITTED" -> List.of("APPEAL_APPROVED", "APPEAL_DENIED").contains(next);
            default -> false;
        };
        if (!valid) throw new IllegalStateException("Invalid PA transition: " + current + " → " + next);
    }

    private PriorAuthorizationDto toDto(PriorAuthorization pa) {
        return new PriorAuthorizationDto(pa.getId(), pa.getAttemptNumber(), pa.getPaType(), pa.getStatus(),
                pa.getPayerName(), pa.getSubmissionMethod(), pa.getSubmittedAt(), pa.getDeterminedAt(),
                pa.getAuthorizationNumber(), pa.getDenialReason(), pa.getAppealDeadline());
    }
}
