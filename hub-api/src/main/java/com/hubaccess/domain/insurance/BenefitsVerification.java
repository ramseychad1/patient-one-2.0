package com.hubaccess.domain.insurance;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "benefits_verification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitsVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "insurance_plan_id")
    private UUID insurancePlanId;

    @Column(name = "verification_type", nullable = false, length = 10)
    private String verificationType;

    @Column(nullable = false, length = 15)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ebv_request_payload", columnDefinition = "JSONB")
    private String ebvRequestPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ebv_response_payload", columnDefinition = "JSONB")
    private String ebvResponsePayload;

    @Column(name = "summary_of_benefits_url", length = 500)
    private String summaryOfBenefitsUrl;

    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "payer_call_rep_name", length = 200)
    private String payerCallRepName;

    @Column(name = "payer_call_reference", length = 100)
    private String payerCallReference;

    @Column(name = "payer_phone_used", length = 20)
    private String payerPhoneUsed;

    @Column(name = "call_duration_minutes")
    private Integer callDurationMinutes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (status == null) status = "PENDING";
    }
}
