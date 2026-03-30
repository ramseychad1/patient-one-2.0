-- V006: InsurancePlan, BenefitsVerification, EnrollmentRecord (Layer 3)

CREATE TABLE insurance_plan (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id                     UUID NOT NULL REFERENCES hub_case(id),
    insurance_type              VARCHAR(20) NOT NULL
                                CHECK (insurance_type IN (
                                    'COMMERCIAL', 'MEDICARE', 'MEDICAID', 'TRICARE',
                                    'VA', 'MEDICARE_ADVANTAGE', 'UNINSURED'
                                )),
    plan_name                   VARCHAR(200),
    payer_name                  VARCHAR(200),
    member_id                   VARCHAR(100),       -- PHI
    group_number                VARCHAR(100),
    bin                         VARCHAR(10),
    pcn                         VARCHAR(20),
    plan_type                   VARCHAR(50),
    benefit_year_type           VARCHAR(10)
                                CHECK (benefit_year_type IN ('CALENDAR', 'BENEFIT')),
    effective_date              DATE,
    termination_date            DATE,
    deductible_individual       DECIMAL(10,2),
    deductible_met              DECIMAL(10,2),
    oop_max_individual          DECIMAL(10,2),
    oop_met                     DECIMAL(10,2),
    copay_drug                  DECIMAL(10,2),
    coinsurance_pct             DECIMAL(5,2),
    formulary_tier              INTEGER,
    pa_required                 BOOLEAN,
    step_therapy_required       BOOLEAN,
    covered_specialty_pharmacies VARCHAR[],
    ebv_source                  VARCHAR(50),
    ebv_run_at                  TIMESTAMP,
    is_primary                  BOOLEAN NOT NULL DEFAULT true,
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE benefits_verification (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id                 UUID NOT NULL REFERENCES hub_case(id),
    insurance_plan_id       UUID REFERENCES insurance_plan(id),
    verification_type       VARCHAR(10) NOT NULL
                            CHECK (verification_type IN ('EBV', 'MANUAL')),
    status                  VARCHAR(15) NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETE', 'FAILED')),
    ebv_request_payload     JSONB,
    ebv_response_payload    JSONB,
    summary_of_benefits_url VARCHAR(500),
    performed_by            UUID REFERENCES hub_user(id),
    payer_call_rep_name     VARCHAR(200),
    payer_call_reference    VARCHAR(100),
    payer_phone_used        VARCHAR(20),
    call_duration_minutes   INTEGER,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at            TIMESTAMP
);

CREATE TABLE enrollment_record (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id                 UUID NOT NULL REFERENCES hub_case(id),
    source_channel          VARCHAR(10) NOT NULL
                            CHECK (source_channel IN ('FAX_PDF', 'ERX', 'DEP')),
    raw_intake_data         JSONB NOT NULL,
    ocr_confidence_score    DECIMAL(5,4),
    erx_transaction_id      VARCHAR(100),
    mi_fields_missing       VARCHAR[],
    mi_resolved_at          TIMESTAMP,
    mi_resolved_by          UUID REFERENCES hub_user(id),
    consent_hipaa           BOOLEAN NOT NULL DEFAULT false,
    consent_program         BOOLEAN NOT NULL DEFAULT false,
    consent_marketing       BOOLEAN NOT NULL DEFAULT false,
    consent_adherence       BOOLEAN NOT NULL DEFAULT false,
    consent_eiv             BOOLEAN NOT NULL DEFAULT false,
    consent_collected_at    TIMESTAMP,
    consent_collected_method VARCHAR(15)
                            CHECK (consent_collected_method IN ('SMS_PORTAL', 'PHONE', 'IN_PERSON')),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW()
);
