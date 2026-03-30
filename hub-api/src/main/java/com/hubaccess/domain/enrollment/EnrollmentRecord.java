package com.hubaccess.domain.enrollment;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrollment_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "source_channel", nullable = false, length = 10)
    private String sourceChannel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_intake_data", nullable = false, columnDefinition = "JSONB")
    private String rawIntakeData;

    @Column(name = "ocr_confidence_score", precision = 5, scale = 4)
    private BigDecimal ocrConfidenceScore;

    @Column(name = "erx_transaction_id", length = 100)
    private String erxTransactionId;

    @Column(name = "mi_fields_missing", columnDefinition = "VARCHAR[]")
    private String[] miFieldsMissing;

    @Column(name = "mi_resolved_at")
    private OffsetDateTime miResolvedAt;

    @Column(name = "mi_resolved_by")
    private UUID miResolvedBy;

    @Column(name = "consent_hipaa", nullable = false)
    private Boolean consentHipaa;

    @Column(name = "consent_program", nullable = false)
    private Boolean consentProgram;

    @Column(name = "consent_marketing", nullable = false)
    private Boolean consentMarketing;

    @Column(name = "consent_adherence", nullable = false)
    private Boolean consentAdherence;

    @Column(name = "consent_eiv", nullable = false)
    private Boolean consentEiv;

    @Column(name = "consent_collected_at")
    private OffsetDateTime consentCollectedAt;

    @Column(name = "consent_collected_method", length = 15)
    private String consentCollectedMethod;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (consentHipaa == null) consentHipaa = false;
        if (consentProgram == null) consentProgram = false;
        if (consentMarketing == null) consentMarketing = false;
        if (consentAdherence == null) consentAdherence = false;
        if (consentEiv == null) consentEiv = false;
    }
}
