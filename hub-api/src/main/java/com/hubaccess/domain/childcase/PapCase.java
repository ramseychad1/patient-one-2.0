package com.hubaccess.domain.childcase;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pap_case")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PapCase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hub_case_id", nullable = false)
    private UUID hubCaseId;

    @Column(name = "pap_case_number", length = 50)
    private String papCaseNumber;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "reason", length = 100)
    private String reason;

    @Column(name = "fpl_percentage", precision = 6, scale = 2)
    private BigDecimal fplPercentage;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "annual_income_usd", precision = 12, scale = 2)
    private BigDecimal annualIncomeUsd;

    @Column(name = "income_verified_method", length = 50)
    private String incomeVerifiedMethod;

    @Column(name = "hardship_waiver_applied")
    private Boolean hardshipWaiverApplied;

    @Column(name = "approval_effective_date")
    private LocalDate approvalEffectiveDate;

    @Column(name = "approval_expiry_date")
    private LocalDate approvalExpiryDate;

    @Column(name = "approval_letter_url", length = 500)
    private String approvalLetterUrl;

    @Column(name = "denial_reason", length = 200)
    private String denialReason;

    @Column(name = "ineligible_reason", length = 200)
    private String ineligibleReason;

    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
