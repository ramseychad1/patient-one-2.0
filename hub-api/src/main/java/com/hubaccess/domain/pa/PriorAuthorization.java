package com.hubaccess.domain.pa;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "prior_authorization")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorAuthorization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "parent_pa_id")
    private UUID parentPaId;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "pa_type", nullable = false, length = 20)
    private String paType;

    @Column(nullable = false, length = 15)
    private String status;

    @Column(name = "payer_name", length = 200)
    private String payerName;

    @Column(name = "payer_pa_phone", length = 20)
    private String payerPaPhone;

    @Column(name = "payer_pa_fax", length = 20)
    private String payerPaFax;

    @Column(name = "submission_method", length = 10)
    private String submissionMethod;

    @Column(name = "epa_transaction_id", length = 100)
    private String epaTransactionId;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "submit_sla_deadline")
    private OffsetDateTime submitSlaDeadline;

    @Column(name = "followup_due_at")
    private OffsetDateTime followupDueAt;

    @Column(name = "determined_at")
    private OffsetDateTime determinedAt;

    @Column(name = "authorization_number", length = 100)
    private String authorizationNumber;

    @Column(name = "authorized_start_date")
    private LocalDate authorizedStartDate;

    @Column(name = "authorized_end_date")
    private LocalDate authorizedEndDate;

    @Column(name = "reauth_task_created_at")
    private OffsetDateTime reauthTaskCreatedAt;

    @Column(name = "denial_reason", length = 500)
    private String denialReason;

    @Column(name = "denial_code", length = 50)
    private String denialCode;

    @Column(name = "appeal_deadline")
    private OffsetDateTime appealDeadline;

    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (attemptNumber == null) attemptNumber = 1;
        if (status == null) status = "PENDING";
    }
}
