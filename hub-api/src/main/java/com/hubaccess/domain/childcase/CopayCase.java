package com.hubaccess.domain.childcase;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "copay_case")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopayCase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hub_case_id", nullable = false)
    private UUID hubCaseId;

    @Column(name = "copay_case_number", length = 50)
    private String copayCaseNumber;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "reason", length = 100)
    private String reason;

    @Column(name = "aks_check_passed")
    private Boolean aksCheckPassed;

    @Column(name = "aks_block_reason", length = 200)
    private String aksBlockReason;

    @Column(name = "card_number", length = 100)
    private String cardNumber;

    @Column(name = "bin", length = 20)
    private String bin;

    @Column(name = "pcn", length = 20)
    private String pcn;

    @Column(name = "group_code", length = 20)
    private String groupCode;

    @Column(name = "max_benefit_usd", precision = 10, scale = 2)
    private BigDecimal maxBenefitUsd;

    @Column(name = "used_ytd_usd", precision = 10, scale = 2)
    private BigDecimal usedYtdUsd;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
