package com.hubaccess.domain.cases;

import com.hubaccess.domain.activity.*;
import com.hubaccess.domain.activity.dto.CaseTaskDto;
import com.hubaccess.domain.activity.dto.TimelineEntryDto;
import com.hubaccess.domain.auth.HubUser;
import com.hubaccess.domain.auth.HubUserRepository;
import com.hubaccess.domain.cases.dto.*;
import com.hubaccess.domain.financial.FinancialAssistanceCase;
import com.hubaccess.domain.financial.FinancialAssistanceCaseRepository;
import com.hubaccess.domain.financial.dto.FinancialAssistanceDto;
import com.hubaccess.domain.insurance.BenefitsVerification;
import com.hubaccess.domain.insurance.BenefitsVerificationRepository;
import com.hubaccess.domain.insurance.InsurancePlan;
import com.hubaccess.domain.insurance.InsurancePlanRepository;
import com.hubaccess.domain.insurance.dto.BenefitsVerificationDto;
import com.hubaccess.domain.insurance.dto.InsurancePlanDto;
import com.hubaccess.domain.outreach.PatientOutreach;
import com.hubaccess.domain.outreach.PatientOutreachRepository;
import com.hubaccess.domain.pa.PriorAuthorization;
import com.hubaccess.domain.pa.PriorAuthorizationRepository;
import com.hubaccess.domain.pa.dto.PriorAuthorizationDto;
import com.hubaccess.domain.patient.Patient;
import com.hubaccess.domain.patient.PatientRepository;
import com.hubaccess.domain.patient.dto.PatientDto;
import com.hubaccess.domain.prescriber.Prescriber;
import com.hubaccess.domain.prescriber.PrescriberRepository;
import com.hubaccess.domain.prescriber.dto.PrescriberDto;
import com.hubaccess.domain.program.Program;
import com.hubaccess.domain.program.ProgramRepository;
import com.hubaccess.domain.workflow.WorkflowState;
import com.hubaccess.domain.workflow.WorkflowStateRepository;
import com.hubaccess.domain.workflow.dto.WorkflowStateDto;
import com.hubaccess.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CaseService {

    private final HubCaseRepository caseRepo;
    private final PatientRepository patientRepo;
    private final PrescriberRepository prescriberRepo;
    private final ProgramRepository programRepo;
    private final HubUserRepository userRepo;
    private final InsurancePlanRepository insurancePlanRepo;
    private final BenefitsVerificationRepository bvRepo;
    private final WorkflowStateRepository workflowStateRepo;
    private final CaseTaskRepository taskRepo;
    private final InteractionRepository interactionRepo;
    private final CaseStatusHistoryRepository statusHistoryRepo;
    private final PatientOutreachRepository outreachRepo;
    private final PriorAuthorizationRepository paRepo;
    private final FinancialAssistanceCaseRepository faRepo;

    @Transactional(readOnly = true)
    public List<CaseListItemDto> listCases(AuthenticatedUser user, String stage, String status,
                                            Boolean slaBreachFlag, String search) {
        List<HubCase> cases;
        if (user.roles().contains("HUB_ADMIN")) {
            cases = caseRepo.findAll();
        } else {
            cases = caseRepo.findAll().stream()
                    .filter(c -> c.getAssignedCmId() != null && c.getAssignedCmId().equals(user.id()))
                    .toList();
        }

        // Filter by active program if set
        if (user.activeProgramId() != null) {
            cases = cases.stream()
                    .filter(c -> c.getProgramId().equals(user.activeProgramId()))
                    .toList();
        }

        Stream<HubCase> stream = cases.stream();
        if (stage != null) stream = stream.filter(c -> stage.equals(c.getCurrentStage()));
        if (status != null) stream = stream.filter(c -> status.equals(c.getCurrentWorkflowState()));
        if (slaBreachFlag != null) stream = stream.filter(c -> slaBreachFlag.equals(c.getSlaBreachFlag()));
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            stream = stream.filter(c -> {
                Patient p = patientRepo.findById(c.getPatientId()).orElse(null);
                return p != null && (p.getFirstName().toLowerCase().contains(q)
                        || p.getLastName().toLowerCase().contains(q)
                        || c.getCaseNumber().toLowerCase().contains(q));
            });
        }

        return stream
                .sorted(Comparator.comparing(HubCase::getSlaBreachFlag, Comparator.reverseOrder())
                        .thenComparing(HubCase::getUpdatedAt, Comparator.reverseOrder()))
                .map(this::toCaseListItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public CaseDetailDto getCaseDetail(UUID caseId) {
        HubCase hc = caseRepo.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));
        return toCaseDetail(hc);
    }

    @Transactional(readOnly = true)
    public List<TimelineEntryDto> getTimeline(UUID caseId, int limit) {
        List<TimelineEntryDto> entries = new ArrayList<>();

        // Status history
        statusHistoryRepo.findByCaseIdOrderByChangedAtDesc(caseId).forEach(sh -> {
            String performer = sh.getChangedBy() != null ? lookupUserName(sh.getChangedBy()) : "System";
            entries.add(new TimelineEntryDto(
                    sh.getId(), "STATUS_CHANGE",
                    sh.getFromState() != null ? sh.getFromState() + " → " + sh.getToState() : "Initial: " + sh.getToState(),
                    sh.getChangeReason(),
                    performer, sh.getChangedAt(),
                    sh.getTriggeredByAction() != null ? Map.of("action", sh.getTriggeredByAction()) : Map.of()
            ));
        });

        // Interactions
        interactionRepo.findByCaseIdOrderByCreatedAtDesc(caseId).forEach(i -> {
            entries.add(new TimelineEntryDto(
                    i.getId(), "INTERACTION",
                    i.getSubject() != null ? i.getSubject() : i.getInteractionType(),
                    i.getBody(),
                    lookupUserName(i.getPerformedBy()), i.getCreatedAt(),
                    buildInteractionMeta(i)
            ));
        });

        // Outreach
        outreachRepo.findByCaseId(caseId).forEach(o -> {
            entries.add(new TimelineEntryDto(
                    o.getId(), "OUTREACH",
                    o.getOutreachType() + " via " + o.getChannel(),
                    o.getMessageBody(),
                    lookupUserName(o.getInitiatedBy()), o.getCreatedAt(),
                    Map.of("deliveryStatus", o.getDeliveryStatus() != null ? o.getDeliveryStatus() : "UNKNOWN")
            ));
        });

        return entries.stream()
                .sorted(Comparator.comparing(TimelineEntryDto::occurredAt).reversed())
                .limit(limit)
                .toList();
    }

    @Transactional
    public CaseDetailDto patchCase(UUID caseId, CasePatchRequest patch, AuthenticatedUser user) {
        HubCase hc = caseRepo.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));

        if (patch.escalationFlag() != null) hc.setEscalationFlag(patch.escalationFlag());
        if (patch.escalationReason() != null) hc.setEscalationReason(patch.escalationReason());
        if (patch.assignedCmId() != null) hc.setAssignedCmId(patch.assignedCmId());

        caseRepo.save(hc);
        return toCaseDetail(hc);
    }

    @Transactional(readOnly = true)
    public DashboardDto getDashboard(AuthenticatedUser user) {
        List<CaseListItemDto> myCases = listCases(user, null, null, null, null);
        List<CaseListItemDto> slaBreaches = myCases.stream()
                .filter(c -> Boolean.TRUE.equals(c.slaBreachFlag()))
                .toList();

        List<CaseTaskDto> openTasks;
        if (user.roles().contains("HUB_ADMIN")) {
            openTasks = taskRepo.findAll().stream()
                    .filter(t -> List.of("OPEN", "IN_PROGRESS").contains(t.getStatus()))
                    .map(this::toTaskDto).toList();
        } else {
            openTasks = taskRepo.findByAssignedToAndStatusIn(user.id(), List.of("OPEN", "IN_PROGRESS"))
                    .stream().map(this::toTaskDto).toList();
        }
        // Filter tasks by active program's cases
        if (user.activeProgramId() != null) {
            java.util.Set<java.util.UUID> programCaseIds = myCases.stream()
                    .map(CaseListItemDto::id).collect(java.util.stream.Collectors.toSet());
            openTasks = openTasks.stream()
                    .filter(t -> programCaseIds.contains(t.caseId()))
                    .toList();
        }

        long totalOpen = myCases.stream().filter(c -> !"CLOSED".equals(c.stage())).count();
        long pendingConsent = myCases.stream().filter(c -> "CONSENT".equals(c.stage())).count();
        long pendingPA = myCases.stream().filter(c -> "PA".equals(c.stage())).count();
        long pendingFA = myCases.stream().filter(c -> "FINANCIAL".equals(c.stage())).count();

        return new DashboardDto(
                myCases, slaBreaches, openTasks,
                new DashboardDto.DashboardStats(totalOpen, slaBreaches.size(), pendingConsent, pendingPA, pendingFA)
        );
    }

    // ─── Mapping helpers ─────────────────────────────────────────────

    CaseListItemDto toCaseListItem(HubCase hc) {
        Patient p = patientRepo.findById(hc.getPatientId()).orElse(null);
        Program prog = programRepo.findById(hc.getProgramId()).orElse(null);
        HubUser cm = hc.getAssignedCmId() != null ? userRepo.findById(hc.getAssignedCmId()).orElse(null) : null;

        return new CaseListItemDto(
                hc.getId(), hc.getCaseNumber(),
                p != null ? p.getFirstName() + " " + p.getLastName() : "Unknown",
                prog != null ? prog.getName() : "Unknown",
                hc.getCurrentStage(), hc.getCurrentWorkflowState(),
                hc.getInsuranceType(),
                hc.getSlaBreachFlag(), hc.getEscalationFlag(),
                cm != null ? cm.getFirstName() + " " + cm.getLastName() : null,
                hc.getCreatedAt(), hc.getUpdatedAt()
        );
    }

    CaseDetailDto toCaseDetail(HubCase hc) {
        Patient p = patientRepo.findById(hc.getPatientId()).orElse(null);
        Prescriber pr = hc.getPrescriberId() != null ? prescriberRepo.findById(hc.getPrescriberId()).orElse(null) : null;
        Program prog = programRepo.findById(hc.getProgramId()).orElse(null);

        InsurancePlan ip = insurancePlanRepo.findByCaseId(hc.getId()).stream()
                .filter(InsurancePlan::getIsPrimary).findFirst().orElse(null);
        BenefitsVerification bv = bvRepo.findByCaseId(hc.getId()).stream()
                .max(Comparator.comparing(BenefitsVerification::getCreatedAt)).orElse(null);
        WorkflowState ws = workflowStateRepo.findByCaseIdAndIsCurrentTrue(hc.getId()).orElse(null);

        List<CaseTaskDto> openTasks = taskRepo.findByCaseId(hc.getId()).stream()
                .filter(t -> "OPEN".equals(t.getStatus()) || "IN_PROGRESS".equals(t.getStatus()))
                .map(this::toTaskDto).toList();

        List<TimelineEntryDto> timeline = getTimeline(hc.getId(), 20);

        List<PriorAuthorizationDto> pas = paRepo.findByCaseIdOrderByAttemptNumberDesc(hc.getId())
                .stream().map(this::toPaDto).toList();
        List<FinancialAssistanceDto> fas = faRepo.findByCaseId(hc.getId())
                .stream().map(this::toFaDto).toList();

        return new CaseDetailDto(
                hc.getId(), hc.getCaseNumber(),
                prog != null ? prog.getName() : null,
                hc.getCurrentStage(), hc.getCurrentWorkflowState(),
                hc.getSourceChannel(), hc.getInsuranceType(),
                hc.getPaRequired(), hc.getPaStatus(),
                hc.getCopayEligible(), hc.getPapEligible(), hc.getPapStatus(),
                hc.getBridgeActive(), hc.getQuickStartActive(),
                hc.getSlaBreachFlag(), hc.getEscalationFlag(), hc.getEscalationReason(),
                hc.getCreatedAt(), hc.getUpdatedAt(),
                p != null ? toPatientDto(p) : null,
                pr != null ? toPrescriberDto(pr) : null,
                ip != null ? toInsurancePlanDto(ip) : null,
                bv != null ? toBvDto(bv) : null,
                ws != null ? toWorkflowStateDto(ws) : null,
                openTasks, timeline, pas, fas
        );
    }

    private PatientDto toPatientDto(Patient p) {
        return new PatientDto(p.getId(), p.getFirstName(), p.getLastName(), p.getDateOfBirth(),
                p.getGender(), p.getAddressLine1(), p.getAddressLine2(), p.getCity(), p.getState(),
                p.getZip(), p.getPhonePrimary(), p.getPhoneSecondary(), p.getEmail(),
                p.getPreferredContactMethod(), p.getPreferredLanguage());
    }

    private PrescriberDto toPrescriberDto(Prescriber pr) {
        return new PrescriberDto(pr.getId(), pr.getNpi(), pr.getFirstName(), pr.getLastName(),
                pr.getPracticeName(), pr.getSpecialty(), pr.getPhone(), pr.getFax());
    }

    private InsurancePlanDto toInsurancePlanDto(InsurancePlan ip) {
        return new InsurancePlanDto(ip.getId(), ip.getInsuranceType(), ip.getPlanName(), ip.getPayerName(),
                ip.getMemberId(), ip.getGroupNumber(), ip.getBin(), ip.getPcn(), ip.getPlanType(),
                ip.getDeductibleIndividual(), ip.getDeductibleMet(), ip.getOopMaxIndividual(), ip.getOopMet(),
                ip.getCopayDrug(), ip.getCoinsurancePct(), ip.getFormularyTier(),
                ip.getPaRequired(), ip.getStepTherapyRequired(), ip.getCoveredSpecialtyPharmacies(),
                ip.getIsPrimary());
    }

    private BenefitsVerificationDto toBvDto(BenefitsVerification bv) {
        return new BenefitsVerificationDto(bv.getId(), bv.getVerificationType(), bv.getStatus(),
                bv.getCreatedAt(), bv.getCompletedAt());
    }

    private WorkflowStateDto toWorkflowStateDto(WorkflowState ws) {
        return new WorkflowStateDto(ws.getState(), ws.getNextRequiredAction(),
                ws.getNextActionLabel(), ws.getNextActionDeadline(), ws.getEnteredAt());
    }

    CaseTaskDto toTaskDto(CaseTask t) {
        return new CaseTaskDto(t.getId(), t.getCaseId(), t.getTaskType(), t.getTitle(), t.getDescription(),
                t.getStatus(), t.getPriority(), t.getActionKey(), t.getSlaBreached(), t.getDueAt(), t.getCreatedAt());
    }

    private PriorAuthorizationDto toPaDto(PriorAuthorization pa) {
        return new PriorAuthorizationDto(pa.getId(), pa.getAttemptNumber(), pa.getPaType(), pa.getStatus(),
                pa.getPayerName(), pa.getSubmissionMethod(), pa.getSubmittedAt(), pa.getDeterminedAt(),
                pa.getAuthorizationNumber(), pa.getDenialReason(), pa.getAppealDeadline());
    }

    private FinancialAssistanceDto toFaDto(FinancialAssistanceCase fa) {
        return new FinancialAssistanceDto(fa.getId(), fa.getFaType(), fa.getStatus(),
                fa.getEffectiveDate(), fa.getExpirationDate(), fa.getCopayMaxBenefitUsd(),
                fa.getCopayUsedYtdUsd(), fa.getPapFplPercentage(), fa.getDenialReason());
    }

    private String lookupUserName(UUID userId) {
        return userRepo.findById(userId)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("Unknown");
    }

    private Map<String, String> buildInteractionMeta(Interaction i) {
        Map<String, String> meta = new HashMap<>();
        if (i.getInteractionType() != null) meta.put("type", i.getInteractionType());
        if (i.getDirection() != null) meta.put("direction", i.getDirection());
        if (i.getChannel() != null) meta.put("channel", i.getChannel());
        if (i.getContactRole() != null) meta.put("contactRole", i.getContactRole());
        if (i.getDurationMinutes() != null) meta.put("durationMinutes", i.getDurationMinutes().toString());
        return meta;
    }
}
