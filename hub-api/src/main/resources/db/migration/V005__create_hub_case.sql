-- V005: HubCase (Layer 2 - Case Core)

CREATE TABLE hub_case (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_number             VARCHAR(20) NOT NULL UNIQUE,
    program_id              UUID NOT NULL REFERENCES program(id),
    patient_id              UUID NOT NULL REFERENCES patient(id),
    prescriber_id           UUID REFERENCES prescriber(id),
    assigned_cm_id          UUID REFERENCES hub_user(id),
    source_channel          VARCHAR(10) NOT NULL
                            CHECK (source_channel IN ('FAX_PDF', 'ERX', 'DEP')),
    current_workflow_state  VARCHAR(30) NOT NULL DEFAULT 'INTAKE_PENDING'
                            CHECK (current_workflow_state IN (
                                'INTAKE_PENDING', 'INTAKE_COMPLETE',
                                'CONSENT_PENDING', 'CONSENT_CONFIRMED',
                                'BI_PENDING', 'BI_IN_PROGRESS', 'BI_COMPLETE',
                                'COPAY_ASSESSMENT', 'COPAY_ENROLLED',
                                'PAP_ASSESSMENT', 'EIV_PENDING', 'EIV_COMPLETE', 'PAP_APPROVED', 'PAP_DENIED',
                                'PA_PENDING', 'PA_SUBMITTED', 'PA_APPROVED', 'PA_DENIED',
                                'APPEAL_PENDING', 'APPEAL_SUBMITTED', 'APPEAL_APPROVED', 'APPEAL_DENIED',
                                'BRIDGE_ACTIVE', 'QUICK_START_ACTIVE',
                                'FINANCIAL_COMPLETE',
                                'TRIAGE_PENDING', 'TRIAGE_COMPLETE',
                                'THERAPY_ACTIVE',
                                'ADHERENCE_MONITORING',
                                'CLOSED'
                            )),
    current_stage           VARCHAR(20) NOT NULL DEFAULT 'INTAKE'
                            CHECK (current_stage IN (
                                'INTAKE', 'CONSENT', 'BI_BV', 'PA', 'FINANCIAL', 'TRIAGE', 'ADHERENCE', 'CLOSED'
                            )),
    insurance_type          VARCHAR(20)
                            CHECK (insurance_type IN ('COMMERCIAL', 'GOVERNMENT', 'UNINSURED')),
    pa_required             BOOLEAN,
    pa_status               VARCHAR(20)
                            CHECK (pa_status IN ('PENDING', 'SUBMITTED', 'APPROVED', 'DENIED', 'APPEALING')),
    copay_eligible          BOOLEAN,
    pap_eligible            BOOLEAN,
    pap_status              VARCHAR(20)
                            CHECK (pap_status IN ('PENDING', 'APPROVED', 'DENIED', 'EXPIRED')),
    bridge_active           BOOLEAN NOT NULL DEFAULT false,
    quick_start_active      BOOLEAN NOT NULL DEFAULT false,
    sla_breach_flag         BOOLEAN NOT NULL DEFAULT false,
    escalation_flag         BOOLEAN NOT NULL DEFAULT false,
    escalation_reason       VARCHAR(500),
    aor_generated_at        TIMESTAMP,
    therapy_start_date      DATE,
    case_close_reason       VARCHAR(500),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Sequence for case number generation (HC-YYYY-NNNNN)
CREATE SEQUENCE case_number_seq START WITH 143;
