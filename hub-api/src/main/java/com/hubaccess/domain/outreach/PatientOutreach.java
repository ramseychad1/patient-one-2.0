package com.hubaccess.domain.outreach;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "patient_outreach")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientOutreach {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "outreach_type", nullable = false, length = 20)
    private String outreachType;

    @Column(nullable = false, length = 10)
    private String channel;

    @Column(name = "recipient_type", nullable = false, length = 15)
    private String recipientType;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Column(name = "recipient_fax", length = 20)
    private String recipientFax;

    @Column(name = "recipient_email", length = 200)
    private String recipientEmail;

    @Column(name = "message_body", columnDefinition = "TEXT")
    private String messageBody;

    @Column(name = "unique_url", length = 500)
    private String uniqueUrl;

    @Column(name = "access_code", length = 20)
    private String accessCode;

    @Column(name = "access_code_expires_at")
    private OffsetDateTime accessCodeExpiresAt;

    @Column(name = "stub_message_id", length = 100)
    private String stubMessageId;

    @Column(name = "delivery_status", length = 10)
    private String deliveryStatus;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "resolved_by_patient")
    private Boolean resolvedByPatient;

    @Column(name = "initiated_by", nullable = false)
    private UUID initiatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
