package com.hubaccess.domain.program;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "program_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "program_id", nullable = false, unique = true)
    private UUID programId;

    @Column(name = "accepted_sources", columnDefinition = "VARCHAR[]")
    private String[] acceptedSources;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mi_required_fields", columnDefinition = "JSONB")
    private String miRequiredFields;

    @Column(name = "mi_sla_business_days")
    private Integer miSlaBusinessDays;

    @Column(name = "consent_method", length = 20)
    private String consentMethod;

    @Column(name = "prior_auth_required")
    private Boolean priorAuthRequired;

    @Column(name = "copay_assistance_enabled")
    private Boolean copayAssistanceEnabled;

    @Column(name = "pap_enabled")
    private Boolean papEnabled;

    @Column(name = "bridge_supply_enabled")
    private Boolean bridgeSupplyEnabled;

    @Column(name = "quick_start_enabled")
    private Boolean quickStartEnabled;

    @Column(name = "rems_tracking_enabled")
    private Boolean remsTrackingEnabled;

    @Column(name = "adherence_program_enabled")
    private Boolean adherenceProgramEnabled;

    @Column(name = "ebv_enabled")
    private Boolean ebvEnabled;

    @Column(name = "eiv_enabled")
    private Boolean eivEnabled;

    @Column(name = "nurse_education_enabled")
    private Boolean nurseEducationEnabled;

    @Column(name = "welcome_kit_enabled")
    private Boolean welcomeKitEnabled;

    @Column(name = "travel_assistance_enabled")
    private Boolean travelAssistanceEnabled;

    @Column(name = "infusion_site_enabled")
    private Boolean infusionSiteEnabled;

    @Column(name = "fpl_threshold_pct")
    private Integer fplThresholdPct;

    @Column(name = "pap_approval_duration_months")
    private Integer papApprovalDurationMonths;

    @Column(name = "pap_reenrollment_lead_days")
    private Integer papReenrollmentLeadDays;

    @Column(name = "copay_max_benefit_usd", precision = 10, scale = 2)
    private BigDecimal copayMaxBenefitUsd;

    @Column(name = "bridge_max_duration_months")
    private Integer bridgeMaxDurationMonths;

    @Column(name = "quick_start_max_duration_months")
    private Integer quickStartMaxDurationMonths;

    @Column(name = "pa_submit_sla_business_days")
    private Integer paSubmitSlaBusinessDays;

    @Column(name = "pa_followup_sla_business_days")
    private Integer paFollowupSlaBusinessDays;

    @Column(name = "pa_appeal_window_days")
    private Integer paAppealWindowDays;

    @Column(name = "pa_max_appeal_levels")
    private Integer paMaxAppealLevels;

    @Column(name = "pa_auto_escalate")
    private Boolean paAutoEscalate;

    @Column(name = "sp_followup_sla_business_days")
    private Integer spFollowupSlaBusinessDays;

    @Column(name = "adherence_checkin_intervals_days", columnDefinition = "INTEGER[]")
    private Integer[] adherenceCheckinIntervalsDays;

    @Column(name = "refill_reminder_lead_days")
    private Integer refillReminderLeadDays;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;
}
