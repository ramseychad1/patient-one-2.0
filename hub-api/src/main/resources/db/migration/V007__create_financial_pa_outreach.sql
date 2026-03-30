-- V007: FinancialAssistanceCase, PriorAuthorization, PatientOutreach (Layer 5)

CREATE TABLE financial_assistance_case (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id                     UUID NOT NULL REFERENCES hub_case(id),
    fa_type                     VARCHAR(20) NOT NULL
                                CHECK (fa_type IN ('COPAY', 'PAP', 'BRIDGE', 'QUICK_START', 'ALTERNATE_FUNDING')),
    status                      VARCHAR(15) NOT NULL DEFAULT 'PENDING'
                                CHECK (status IN ('PENDING', 'APPROVED', 'DENIED', 'ACTIVE', 'EXPIRED', 'CLOSED')),
    effective_date              DATE,
    expiration_date             DATE,
    copay_card_number           VARCHAR(100),       -- PHI, COPAY only
    copay_bin                   VARCHAR(10),
    copay_pcn                   VARCHAR(20),
    copay_group                 VARCHAR(50),
    copay_max_benefit_usd       DECIMAL(10,2),
    copay_used_ytd_usd          DECIMAL(10,2) DEFAULT 0,
    pap_fpl_percentage          DECIMAL(5,2),
    pap_income_verified_method  VARCHAR(20)
                                CHECK (pap_income_verified_method IN ('EIV', 'PAYSTUB', 'TAX_RETURN', 'ATTESTATION')),
    pap_approval_letter_url     VARCHAR(500),
    shps_order_id               VARCHAR(100),
    shps_tracking_number        VARCHAR(100),
    shps_dispense_date          DATE,
    denial_reason               VARCHAR(500),
    appeal_requested            BOOLEAN NOT NULL DEFAULT false,
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by                  UUID REFERENCES hub_user(id),
    updated_at                  TIMESTAMP
);

CREATE TABLE prior_authorization (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id                 UUID NOT NULL REFERENCES hub_case(id),
    parent_pa_id            UUID REFERENCES prior_authorization(id),
    attempt_number          INTEGER NOT NULL DEFAULT 1,
    pa_type                 VARCHAR(20) NOT NULL
                            CHECK (pa_type IN ('INITIAL', 'APPEAL_1', 'APPEAL_2', 'EXTERNAL_REVIEW')),
    status                  VARCHAR(15) NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'SUBMITTED', 'APPROVED', 'DENIED', 'WITHDRAWN')),
    payer_name              VARCHAR(200),
    payer_pa_phone          VARCHAR(20),
    payer_pa_fax            VARCHAR(20),
    submission_method       VARCHAR(10)
                            CHECK (submission_method IN ('EPA', 'FAX', 'PHONE', 'PORTAL')),
    epa_transaction_id      VARCHAR(100),
    submitted_at            TIMESTAMP,
    submit_sla_deadline     TIMESTAMP,
    followup_due_at         TIMESTAMP,
    determined_at           TIMESTAMP,
    authorization_number    VARCHAR(100),
    authorized_start_date   DATE,
    authorized_end_date     DATE,
    reauth_task_created_at  TIMESTAMP,
    denial_reason           VARCHAR(500),
    denial_code             VARCHAR(50),
    appeal_deadline         TIMESTAMP,
    performed_by            UUID REFERENCES hub_user(id),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);

CREATE TABLE patient_outreach (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id                 UUID NOT NULL REFERENCES hub_case(id),
    outreach_type           VARCHAR(20) NOT NULL
                            CHECK (outreach_type IN (
                                'MI_REQUEST', 'CONSENT_REQUEST', 'BI_SUMMARY', 'PA_STATUS',
                                'SP_TRIAGE', 'REFILL_REMINDER', 'CHECKIN', 'PAP_REENROLLMENT', 'TRACKING'
                            )),
    channel                 VARCHAR(10) NOT NULL
                            CHECK (channel IN ('SMS', 'FAX', 'EMAIL', 'PHONE')),
    recipient_type          VARCHAR(15) NOT NULL
                            CHECK (recipient_type IN ('PATIENT', 'HCP', 'SP')),
    recipient_phone         VARCHAR(20),            -- PHI
    recipient_fax           VARCHAR(20),
    recipient_email         VARCHAR(200),            -- PHI
    message_body            TEXT,
    unique_url              VARCHAR(500),
    access_code             VARCHAR(20),
    access_code_expires_at  TIMESTAMP,
    stub_message_id         VARCHAR(100),
    delivery_status         VARCHAR(10)
                            CHECK (delivery_status IN ('SENT', 'DELIVERED', 'FAILED', 'MOCK')),
    resolved_at             TIMESTAMP,
    resolved_by_patient     BOOLEAN,
    initiated_by            UUID NOT NULL REFERENCES hub_user(id),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW()
);
