package com.hubaccess.domain.insurance;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "insurance_plan")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsurancePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "insurance_type", nullable = false, length = 20)
    private String insuranceType;

    @Column(name = "plan_name", length = 200)
    private String planName;

    @Column(name = "payer_name", length = 200)
    private String payerName;

    @Column(name = "member_id", length = 100)
    private String memberId;

    @Column(name = "group_number", length = 100)
    private String groupNumber;

    @Column(length = 10)
    private String bin;

    @Column(length = 20)
    private String pcn;

    @Column(name = "plan_type", length = 50)
    private String planType;

    @Column(name = "benefit_year_type", length = 10)
    private String benefitYearType;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "deductible_individual", precision = 10, scale = 2)
    private BigDecimal deductibleIndividual;

    @Column(name = "deductible_met", precision = 10, scale = 2)
    private BigDecimal deductibleMet;

    @Column(name = "oop_max_individual", precision = 10, scale = 2)
    private BigDecimal oopMaxIndividual;

    @Column(name = "oop_met", precision = 10, scale = 2)
    private BigDecimal oopMet;

    @Column(name = "copay_drug", precision = 10, scale = 2)
    private BigDecimal copayDrug;

    @Column(name = "coinsurance_pct", precision = 5, scale = 2)
    private BigDecimal coinsurancePct;

    @Column(name = "formulary_tier")
    private Integer formularyTier;

    @Column(name = "pa_required")
    private Boolean paRequired;

    @Column(name = "step_therapy_required")
    private Boolean stepTherapyRequired;

    @Column(name = "covered_specialty_pharmacies", columnDefinition = "VARCHAR[]")
    private String[] coveredSpecialtyPharmacies;

    @Column(name = "ebv_source", length = 50)
    private String ebvSource;

    @Column(name = "ebv_run_at")
    private OffsetDateTime ebvRunAt;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (isPrimary == null) isPrimary = true;
    }
}
