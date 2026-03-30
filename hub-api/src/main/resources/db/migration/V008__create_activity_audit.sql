-- V008: WorkflowState, CaseTask, Interaction, CaseStatusHistory, ServiceCallLog (Layers 4+6)

CREATE TABLE workflow_state (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id                 UUID NOT NULL REFERENCES hub_case(id),
    state                   VARCHAR(30) NOT NULL,
    entered_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    exited_at               TIMESTAMP,
    triggered_by_action     VARCHAR(100),
    triggered_by_user       UUID REFERENCES hub_user(id),
    triggered_by_stub       VARCHAR(100),
    next_required_action    VARCHAR(100),
    next_action_label       VARCHAR(200),
    next_action_deadline    TIMESTAMP,
    is_current              BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE case_task (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id             UUID NOT NULL REFERENCES hub_case(id),
    task_type           VARCHAR(30) NOT NULL,
    title               VARCHAR(300) NOT NULL,
    description         TEXT,
    status              VARCHAR(15) NOT NULL DEFAULT 'OPEN'
                        CHECK (status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    priority            VARCHAR(10) NOT NULL DEFAULT 'MEDIUM'
                        CHECK (priority IN ('HIGH', 'MEDIUM', 'LOW')),
    assigned_to         UUID REFERENCES hub_user(id),
    due_at              TIMESTAMP,
    sla_breached        BOOLEAN NOT NULL DEFAULT false,
    action_key          VARCHAR(100),
    completed_at        TIMESTAMP,
    completed_by        UUID REFERENCES hub_user(id),
    completion_notes    VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          UUID REFERENCES hub_user(id)
);

CREATE TABLE interaction (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id                 UUID NOT NULL REFERENCES hub_case(id),
    interaction_type        VARCHAR(25) NOT NULL
                            CHECK (interaction_type IN (
                                'NOTE', 'CALL', 'FAX_SENT', 'FAX_RECEIVED',
                                'EMAIL_SENT', 'EMAIL_RECEIVED', 'SMS_SENT',
                                'DOCUMENT_UPLOADED', 'AOR_GENERATED', 'BENEFIT_SUMMARY_SENT',
                                'PA_PACKAGE_SENT', 'APPEAL_PACKAGE_SENT', 'SP_TRIAGE_SENT',
                                'PAP_APPROVAL_SENT', 'PAP_DENIAL_SENT', 'SYSTEM_EVENT', 'ADVERSE_EVENT'
                            )),
    direction               VARCHAR(10)
                            CHECK (direction IN ('INBOUND', 'OUTBOUND', 'INTERNAL')),
    channel                 VARCHAR(10)
                            CHECK (channel IN ('PHONE', 'SMS', 'FAX', 'EMAIL', 'PORTAL', 'SYSTEM')),
    contact_name            VARCHAR(200),
    contact_role            VARCHAR(15)
                            CHECK (contact_role IN ('PATIENT', 'HCP', 'PAYER', 'SP', 'MANUFACTURER')),
    subject                 VARCHAR(300),
    body                    TEXT,
    document_url            VARCHAR(500),
    duration_minutes        INTEGER,
    adverse_event_reported  BOOLEAN NOT NULL DEFAULT false,
    adverse_event_detail    TEXT,
    performed_by            UUID NOT NULL REFERENCES hub_user(id),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE case_status_history (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id                 UUID NOT NULL REFERENCES hub_case(id),
    from_state              VARCHAR(30),
    to_state                VARCHAR(30) NOT NULL,
    from_stage              VARCHAR(20),
    to_stage                VARCHAR(20),
    triggered_by_action     VARCHAR(100),
    changed_by              UUID REFERENCES hub_user(id),
    changed_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    change_reason           VARCHAR(500)
);

CREATE TABLE service_call_log (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id             UUID REFERENCES hub_case(id),
    service_name        VARCHAR(100) NOT NULL,
    method_name         VARCHAR(100) NOT NULL,
    is_stub             BOOLEAN NOT NULL DEFAULT true,
    request_payload     JSONB,
    response_payload    JSONB,
    http_status         INTEGER,
    latency_ms          INTEGER,
    success             BOOLEAN NOT NULL,
    error_message       VARCHAR(500),
    demo_scenario       VARCHAR(100),
    called_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    called_by_user      UUID REFERENCES hub_user(id)
);
