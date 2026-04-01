-- SESSION 14: Child case stub tables for ConnectSource-style case structure
-- Additive only — no existing tables modified.

-- 1. bi_case — Benefit Investigation Case
CREATE TABLE bi_case (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hub_case_id             UUID NOT NULL REFERENCES hub_case(id),
    bi_case_number          VARCHAR(50),
    bi_type                 VARCHAR(50),
    status                  VARCHAR(50) NOT NULL,
    reason                  VARCHAR(100),
    obc_attempt_1_at        TIMESTAMP,
    obc_attempt_1_result    VARCHAR(50),
    obc_attempt_2_at        TIMESTAMP,
    obc_attempt_2_result    VARCHAR(50),
    obc_attempt_3_at        TIMESTAMP,
    obc_attempt_3_result    VARCHAR(50),
    sms_sent_at             TIMESTAMP,
    fax_to_hcp_sent_at      TIMESTAMP,
    coverage_outcome        VARCHAR(100),
    plan_type               VARCHAR(50),
    formulary_tier          INTEGER,
    pa_required             BOOLEAN,
    performed_by            UUID REFERENCES hub_user(id),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);

-- 2. pap_case — Patient Assistance Program Case
CREATE TABLE pap_case (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hub_case_id             UUID NOT NULL REFERENCES hub_case(id),
    pap_case_number         VARCHAR(50),
    status                  VARCHAR(50) NOT NULL,
    reason                  VARCHAR(100),
    fpl_percentage          NUMERIC(6,2),
    household_size          INTEGER,
    annual_income_usd       NUMERIC(12,2),
    income_verified_method  VARCHAR(50),
    hardship_waiver_applied BOOLEAN DEFAULT FALSE,
    approval_effective_date DATE,
    approval_expiry_date    DATE,
    approval_letter_url     VARCHAR(500),
    denial_reason           VARCHAR(200),
    ineligible_reason       VARCHAR(200),
    performed_by            UUID REFERENCES hub_user(id),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);

-- 3. copay_case — Copay Assistance Case
CREATE TABLE copay_case (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hub_case_id             UUID NOT NULL REFERENCES hub_case(id),
    copay_case_number       VARCHAR(50),
    status                  VARCHAR(50) NOT NULL,
    reason                  VARCHAR(100),
    aks_check_passed        BOOLEAN,
    aks_block_reason        VARCHAR(200),
    card_number             VARCHAR(100),
    bin                     VARCHAR(20),
    pcn                     VARCHAR(20),
    group_code              VARCHAR(20),
    max_benefit_usd         NUMERIC(10,2),
    used_ytd_usd            NUMERIC(10,2) DEFAULT 0,
    effective_date          DATE,
    expiration_date         DATE,
    performed_by            UUID REFERENCES hub_user(id),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);

-- 4. ae_case — Adverse Event / Product Complaint Case
CREATE TABLE ae_case (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hub_case_id                 UUID NOT NULL REFERENCES hub_case(id),
    ae_case_number              VARCHAR(50),
    ae_type                     VARCHAR(50) NOT NULL,
    status                      VARCHAR(50) NOT NULL,
    submission_status           VARCHAR(50),
    reported_by                 VARCHAR(100),
    event_description           TEXT,
    event_date                  DATE,
    manufacturer_notified_at    TIMESTAMP,
    sla_1bd_deadline            TIMESTAMP,
    sla_3cd_deadline            TIMESTAMP,
    sla_breached                BOOLEAN DEFAULT FALSE,
    performed_by                UUID REFERENCES hub_user(id),
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP
);

-- 5. missing_information — First-class MI object
CREATE TABLE missing_information (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hub_case_id             UUID NOT NULL REFERENCES hub_case(id),
    mi_number               VARCHAR(50),
    category                VARCHAR(100) NOT NULL,
    detail                  VARCHAR(200) NOT NULL,
    mi_type                 VARCHAR(20) NOT NULL DEFAULT 'Optional',
    reported_date           DATE NOT NULL DEFAULT CURRENT_DATE,
    received_date           DATE,
    resolved_by             UUID REFERENCES hub_user(id),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_bi_case_hub_case ON bi_case(hub_case_id);
CREATE INDEX idx_pap_case_hub_case ON pap_case(hub_case_id);
CREATE INDEX idx_copay_case_hub_case ON copay_case(hub_case_id);
CREATE INDEX idx_ae_case_hub_case ON ae_case(hub_case_id);
CREATE INDEX idx_missing_info_hub_case ON missing_information(hub_case_id);

-- ── SEED DATA for demo case HC-2025-00142 ──

-- BI Case seed
INSERT INTO bi_case (
    hub_case_id, bi_case_number, bi_type, status, reason,
    obc_attempt_1_at, obc_attempt_1_result,
    sms_sent_at, fax_to_hcp_sent_at,
    coverage_outcome, plan_type, formulary_tier, pa_required, updated_at
) SELECT
    id, 'BI-2025-00142-01', 'Initial', 'Completed', 'CoverageConfirmed',
    NOW() - INTERVAL '16 days', 'Reached',
    NOW() - INTERVAL '16 days', NOW() - INTERVAL '16 days',
    'InsuranceCoverage', 'Commercial', 3, TRUE, NOW()
FROM hub_case WHERE case_number = 'HC-2025-00142';

-- Copay Case seed (pending — waiting PA)
INSERT INTO copay_case (
    hub_case_id, copay_case_number, status, reason,
    aks_check_passed, max_benefit_usd, used_ytd_usd, updated_at
) SELECT
    id, 'COPAY-2025-00142-01', 'Pending', 'WaitingPAResolution',
    TRUE, 15000.00, 0.00, NOW()
FROM hub_case WHERE case_number = 'HC-2025-00142';

-- PAP Case seed (not applicable)
INSERT INTO pap_case (
    hub_case_id, pap_case_number, status, reason,
    ineligible_reason, updated_at
) SELECT
    id, 'PAP-2025-00142-01', 'Cancelled', 'NotApplicable',
    'Patient has active commercial insurance', NOW()
FROM hub_case WHERE case_number = 'HC-2025-00142';

-- MI seed records
INSERT INTO missing_information (hub_case_id, mi_number, category, detail, mi_type, reported_date, received_date)
SELECT id, 'MI-000041', 'Authorization', 'Insurance Authorization Documentation', 'Required', CURRENT_DATE - 16, CURRENT_DATE - 15
FROM hub_case WHERE case_number = 'HC-2025-00142';

INSERT INTO missing_information (hub_case_id, mi_number, category, detail, mi_type, reported_date, received_date)
SELECT id, 'MI-000042', 'Clinical', 'Diagnosis Code (ICD-10)', 'Required', CURRENT_DATE - 17, CURRENT_DATE - 16
FROM hub_case WHERE case_number = 'HC-2025-00142';

INSERT INTO missing_information (hub_case_id, mi_number, category, detail, mi_type, reported_date, received_date)
SELECT id, 'MI-000043', 'Other', 'Marketing — SMS Consent', 'Optional', CURRENT_DATE - 16, NULL
FROM hub_case WHERE case_number = 'HC-2025-00142';
