package com.hubaccess.domain.childcase;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bi_case")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiCase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hub_case_id", nullable = false)
    private UUID hubCaseId;

    @Column(name = "bi_case_number", length = 50)
    private String biCaseNumber;

    @Column(name = "bi_type", length = 50)
    private String biType;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "reason", length = 100)
    private String reason;

    @Column(name = "obc_attempt_1_at")
    private OffsetDateTime obcAttempt1At;

    @Column(name = "obc_attempt_1_result", length = 50)
    private String obcAttempt1Result;

    @Column(name = "obc_attempt_2_at")
    private OffsetDateTime obcAttempt2At;

    @Column(name = "obc_attempt_2_result", length = 50)
    private String obcAttempt2Result;

    @Column(name = "obc_attempt_3_at")
    private OffsetDateTime obcAttempt3At;

    @Column(name = "obc_attempt_3_result", length = 50)
    private String obcAttempt3Result;

    @Column(name = "sms_sent_at")
    private OffsetDateTime smsSentAt;

    @Column(name = "fax_to_hcp_sent_at")
    private OffsetDateTime faxToHcpSentAt;

    @Column(name = "coverage_outcome", length = 100)
    private String coverageOutcome;

    @Column(name = "plan_type", length = 50)
    private String planType;

    @Column(name = "formulary_tier")
    private Integer formularyTier;

    @Column(name = "pa_required")
    private Boolean paRequired;

    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
