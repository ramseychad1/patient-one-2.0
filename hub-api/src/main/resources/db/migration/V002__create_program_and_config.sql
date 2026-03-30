-- V002: Program and ProgramConfig tables (Layer 1 - Tenant)

CREATE TABLE program (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    manufacturer_id   UUID NOT NULL REFERENCES manufacturer(id),
    name              VARCHAR(200) NOT NULL,
    drug_brand_name   VARCHAR(200) NOT NULL,
    drug_generic_name VARCHAR(200),
    ndc_codes         VARCHAR[],
    therapeutic_area  VARCHAR(200),
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                      CHECK (status IN ('ACTIVE', 'INACTIVE', 'PILOT')),
    program_start_date DATE,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by        UUID
);

CREATE TABLE program_config (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    program_id                      UUID NOT NULL UNIQUE REFERENCES program(id),
    accepted_sources                VARCHAR[] DEFAULT ARRAY['FAX_PDF','ERX','DEP'],
    mi_required_fields              JSONB,
    mi_sla_business_days            INTEGER DEFAULT 5,
    consent_method                  VARCHAR(20) DEFAULT 'SMS'
                                    CHECK (consent_method IN ('SMS', 'PORTAL', 'PHONE')),
    prior_auth_required             BOOLEAN DEFAULT true,
    copay_assistance_enabled        BOOLEAN DEFAULT true,
    pap_enabled                     BOOLEAN DEFAULT true,
    bridge_supply_enabled           BOOLEAN DEFAULT true,
    quick_start_enabled             BOOLEAN DEFAULT true,
    rems_tracking_enabled           BOOLEAN DEFAULT false,
    adherence_program_enabled       BOOLEAN DEFAULT true,
    ebv_enabled                     BOOLEAN DEFAULT true,
    eiv_enabled                     BOOLEAN DEFAULT true,
    nurse_education_enabled         BOOLEAN DEFAULT false,
    welcome_kit_enabled             BOOLEAN DEFAULT false,
    travel_assistance_enabled       BOOLEAN DEFAULT false,
    infusion_site_enabled           BOOLEAN DEFAULT false,
    fpl_threshold_pct               INTEGER DEFAULT 400,
    pap_approval_duration_months    INTEGER DEFAULT 12,
    pap_reenrollment_lead_days      INTEGER DEFAULT 90,
    copay_max_benefit_usd           DECIMAL(10,2),
    bridge_max_duration_months      INTEGER DEFAULT 6,
    quick_start_max_duration_months INTEGER DEFAULT 12,
    pa_submit_sla_business_days     INTEGER DEFAULT 3,
    pa_followup_sla_business_days   INTEGER DEFAULT 5,
    pa_appeal_window_days           INTEGER DEFAULT 30,
    pa_max_appeal_levels            INTEGER DEFAULT 2,
    pa_auto_escalate                BOOLEAN DEFAULT true,
    sp_followup_sla_business_days   INTEGER DEFAULT 5,
    adherence_checkin_intervals_days INTEGER[] DEFAULT ARRAY[30,60,90],
    refill_reminder_lead_days       INTEGER DEFAULT 7,
    updated_at                      TIMESTAMP,
    updated_by                      UUID
);
