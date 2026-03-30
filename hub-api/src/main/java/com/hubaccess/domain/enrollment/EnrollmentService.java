package com.hubaccess.domain.enrollment;

import com.hubaccess.domain.activity.CaseStatusHistory;
import com.hubaccess.domain.activity.CaseStatusHistoryRepository;
import com.hubaccess.domain.activity.CaseTask;
import com.hubaccess.domain.activity.CaseTaskRepository;
import com.hubaccess.domain.activity.Interaction;
import com.hubaccess.domain.activity.InteractionRepository;
import com.hubaccess.domain.cases.CaseService;
import com.hubaccess.domain.cases.HubCase;
import com.hubaccess.domain.cases.HubCaseRepository;
import com.hubaccess.domain.cases.dto.CaseDetailDto;
import com.hubaccess.domain.enrollment.dto.EnrollmentRequest;
import com.hubaccess.domain.insurance.InsurancePlan;
import com.hubaccess.domain.insurance.InsurancePlanRepository;
import com.hubaccess.domain.patient.Patient;
import com.hubaccess.domain.patient.PatientRepository;
import com.hubaccess.domain.prescriber.Prescriber;
import com.hubaccess.domain.prescriber.PrescriberRepository;
import com.hubaccess.domain.workflow.WorkflowState;
import com.hubaccess.domain.workflow.WorkflowStateRepository;
import com.hubaccess.security.AuthenticatedUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final HubCaseRepository caseRepo;
    private final PatientRepository patientRepo;
    private final PrescriberRepository prescriberRepo;
    private final EnrollmentRecordRepository enrollmentRepo;
    private final InsurancePlanRepository insurancePlanRepo;
    private final WorkflowStateRepository workflowStateRepo;
    private final CaseStatusHistoryRepository statusHistoryRepo;
    private final CaseTaskRepository taskRepo;
    private final InteractionRepository interactionRepo;
    private final CaseService caseService;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public CaseDetailDto createCase(EnrollmentRequest req, AuthenticatedUser user) {
        // 1. Find or create Patient
        Patient patient = findOrCreatePatient(req.patient());

        // 2. Find or create Prescriber
        UUID prescriberId = null;
        if (req.prescriber() != null && req.prescriber().npi() != null) {
            prescriberId = findOrCreatePrescriber(req.prescriber()).getId();
        }

        // 3. Generate case number
        String caseNumber = generateCaseNumber();

        // 4. Determine source channel
        String sourceChannel = switch (req.enrollmentSource()) {
            case "ERX" -> "ERX";
            case "FAX_PDF" -> "FAX_PDF";
            default -> "DEP";
        };

        // 5. Create HubCase
        boolean hasMi = req.miFlags() != null && !req.miFlags().isEmpty();
        String initialState = hasMi ? "INTAKE_PENDING" : "CONSENT_PENDING";
        String initialStage = hasMi ? "INTAKE" : "CONSENT";

        HubCase hc = HubCase.builder()
                .caseNumber(caseNumber)
                .programId(req.programId())
                .patientId(patient.getId())
                .prescriberId(prescriberId)
                .assignedCmId(user.id())
                .sourceChannel(sourceChannel)
                .currentWorkflowState(initialState)
                .currentStage(initialStage)
                .aorGeneratedAt(OffsetDateTime.now())
                .build();
        caseRepo.save(hc);

        // 6. Create EnrollmentRecord
        String rawData = "{\"source\":\"" + req.enrollmentSource() + "\",\"patient\":\"" + patient.getFirstName() + " " + patient.getLastName() + "\"}";
        EnrollmentRecord er = EnrollmentRecord.builder()
                .caseId(hc.getId())
                .sourceChannel(sourceChannel)
                .rawIntakeData(rawData)
                .erxTransactionId(req.erxTransactionId())
                .miFieldsMissing(req.miFlags() != null ? req.miFlags().toArray(new String[0]) : null)
                .build();
        enrollmentRepo.save(er);

        // 7. Create insurance plan if provided
        if (req.insurance() != null && req.insurance().insuranceType() != null) {
            insurancePlanRepo.save(InsurancePlan.builder()
                    .caseId(hc.getId())
                    .insuranceType(req.insurance().insuranceType())
                    .planName(req.insurance().planName())
                    .memberId(req.insurance().memberId())
                    .groupNumber(req.insurance().groupNumber())
                    .isPrimary(true)
                    .build());
        }

        // 8. Create workflow state
        String nextAction = hasMi ? "RESOLVE_MI" : "SEND_CONSENT_SMS";
        String nextLabel = hasMi ? "Resolve missing information" : "Send consent request";
        workflowStateRepo.save(WorkflowState.builder()
                .caseId(hc.getId())
                .state(initialState)
                .triggeredByAction("CREATE_CASE")
                .triggeredByUser(user.id())
                .nextRequiredAction(nextAction)
                .nextActionLabel(nextLabel)
                .isCurrent(true)
                .build());

        // 9. Status history
        statusHistoryRepo.save(CaseStatusHistory.builder()
                .caseId(hc.getId())
                .toState(initialState)
                .toStage(initialStage)
                .triggeredByAction("SUBMIT_" + sourceChannel + "_ENROLLMENT")
                .changedBy(user.id())
                .changeReason("Case created from " + req.enrollmentSource() + " enrollment")
                .build());

        // 10. AOR interaction
        interactionRepo.save(Interaction.builder()
                .caseId(hc.getId())
                .interactionType("AOR_GENERATED")
                .direction("OUTBOUND")
                .channel("SYSTEM")
                .subject("Case created — " + caseNumber)
                .body("Acknowledgment of Receipt generated. Source: " + req.enrollmentSource())
                .performedBy(user.id())
                .build());

        // 11. MI tasks if needed
        if (hasMi) {
            for (String field : req.miFlags()) {
                taskRepo.save(CaseTask.builder()
                        .caseId(hc.getId())
                        .taskType("RESOLVE_MI")
                        .title("Resolve missing field: " + field)
                        .description("Missing required field '" + field + "' from " + req.enrollmentSource() + " enrollment")
                        .priority("HIGH")
                        .assignedTo(user.id())
                        .actionKey("RESOLVE_MI")
                        .dueAt(OffsetDateTime.now().plusDays(5))
                        .build());
            }
        }

        return caseService.getCaseDetail(hc.getId());
    }

    private Patient findOrCreatePatient(EnrollmentRequest.PatientInfo pi) {
        List<Patient> existing = patientRepo.findByLastNameAndFirstNameAndDateOfBirth(
                pi.lastName(), pi.firstName(), pi.dob());
        if (!existing.isEmpty()) return existing.getFirst();

        return patientRepo.save(Patient.builder()
                .firstName(pi.firstName())
                .lastName(pi.lastName())
                .dateOfBirth(pi.dob())
                .phonePrimary(pi.phone())
                .preferredContactMethod(pi.preferredContactMethod())
                .build());
    }

    private Prescriber findOrCreatePrescriber(EnrollmentRequest.PrescriberInfo pi) {
        return prescriberRepo.findByNpi(pi.npi())
                .orElseGet(() -> prescriberRepo.save(Prescriber.builder()
                        .npi(pi.npi())
                        .firstName(pi.firstName() != null ? pi.firstName() : "Unknown")
                        .lastName(pi.lastName() != null ? pi.lastName() : "Unknown")
                        .practiceName(pi.practiceName())
                        .phone(pi.phone())
                        .fax(pi.fax())
                        .build()));
    }

    private String generateCaseNumber() {
        int year = LocalDate.now().getYear();
        Long seq = (Long) em.createNativeQuery("SELECT nextval('case_number_seq')").getSingleResult();
        return String.format("HC-%d-%05d", year, seq);
    }
}
