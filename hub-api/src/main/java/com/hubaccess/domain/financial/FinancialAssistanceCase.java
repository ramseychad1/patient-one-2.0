package com.hubaccess.domain.financial;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "financial_assistance_case")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialAssistanceCase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "fa_type", nullable = false, length = 20)
    private String faType;

    @Column(nullable = false, length = 15)
    private String status;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "copay_card_number", length = 100)
    private String copayCardNumber;

    @Column(name = "copay_bin", length = 10)
    private String copayBin;

    @Column(name = "copay_pcn", length = 20)
    private String copayPcn;

    @Column(name = "copay_group", length = 50)
    private String copayGroup;

    @Column(name = "copay_max_benefit_usd", precision = 10, scale = 2)
    private BigDecimal copayMaxBenefitUsd;

    @Column(name = "copay_used_ytd_usd", precision = 10, scale = 2)
    private BigDecimal copayUsedYtdUsd;

    @Column(name = "pap_fpl_percentage", precision = 5, scale = 2)
    private BigDecimal papFplPercentage;

    @Column(name = "pap_income_verified_method", length = 20)
    private String papIncomeVerifiedMethod;

    @Column(name = "pap_approval_letter_url", length = 500)
    private String papApprovalLetterUrl;

    @Column(name = "shps_order_id", length = 100)
    private String shpsOrderId;

    @Column(name = "shps_tracking_number", length = 100)
    private String shpsTrackingNumber;

    @Column(name = "shps_dispense_date")
    private LocalDate shpsDispenseDate;

    @Column(name = "denial_reason", length = 500)
    private String denialReason;

    @Column(name = "appeal_requested", nullable = false)
    private Boolean appealRequested;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (status == null) status = "PENDING";
        if (appealRequested == null) appealRequested = false;
    }
}
