package com.hubaccess.domain.cases;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "hub_case")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubCase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_number", nullable = false, unique = true, length = 20)
    private String caseNumber;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "prescriber_id")
    private UUID prescriberId;

    @Column(name = "assigned_cm_id")
    private UUID assignedCmId;

    @Column(name = "source_channel", nullable = false, length = 10)
    private String sourceChannel;

    @Column(name = "current_workflow_state", nullable = false, length = 30)
    private String currentWorkflowState;

    @Column(name = "current_stage", nullable = false, length = 20)
    private String currentStage;

    @Column(name = "insurance_type", length = 20)
    private String insuranceType;

    @Column(name = "pa_required")
    private Boolean paRequired;

    @Column(name = "pa_status", length = 20)
    private String paStatus;

    @Column(name = "copay_eligible")
    private Boolean copayEligible;

    @Column(name = "pap_eligible")
    private Boolean papEligible;

    @Column(name = "pap_status", length = 20)
    private String papStatus;

    @Column(name = "bridge_active", nullable = false)
    private Boolean bridgeActive;

    @Column(name = "quick_start_active", nullable = false)
    private Boolean quickStartActive;

    @Column(name = "sla_breach_flag", nullable = false)
    private Boolean slaBreachFlag;

    @Column(name = "escalation_flag", nullable = false)
    private Boolean escalationFlag;

    @Column(name = "escalation_reason", length = 500)
    private String escalationReason;

    @Column(name = "aor_generated_at")
    private OffsetDateTime aorGeneratedAt;

    @Column(name = "therapy_start_date")
    private LocalDate therapyStartDate;

    @Column(name = "case_close_reason", length = 500)
    private String caseCloseReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
        if (currentWorkflowState == null) currentWorkflowState = "INTAKE_PENDING";
        if (currentStage == null) currentStage = "INTAKE";
        if (bridgeActive == null) bridgeActive = false;
        if (quickStartActive == null) quickStartActive = false;
        if (slaBreachFlag == null) slaBreachFlag = false;
        if (escalationFlag == null) escalationFlag = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
