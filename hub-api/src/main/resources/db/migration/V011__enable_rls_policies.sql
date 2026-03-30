-- V011: Row-Level Security policies
-- RLS enforces program isolation at the database layer (defense-in-depth with Spring Security RBAC)

-- Enable RLS on all case-related tables
ALTER TABLE hub_case ENABLE ROW LEVEL SECURITY;
ALTER TABLE insurance_plan ENABLE ROW LEVEL SECURITY;
ALTER TABLE benefits_verification ENABLE ROW LEVEL SECURITY;
ALTER TABLE enrollment_record ENABLE ROW LEVEL SECURITY;
ALTER TABLE workflow_state ENABLE ROW LEVEL SECURITY;
ALTER TABLE financial_assistance_case ENABLE ROW LEVEL SECURITY;
ALTER TABLE prior_authorization ENABLE ROW LEVEL SECURITY;
ALTER TABLE patient_outreach ENABLE ROW LEVEL SECURITY;
ALTER TABLE case_task ENABLE ROW LEVEL SECURITY;
ALTER TABLE interaction ENABLE ROW LEVEL SECURITY;
ALTER TABLE case_status_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE service_call_log ENABLE ROW LEVEL SECURITY;

-- Case-level isolation: CM can only see cases in assigned programs
CREATE POLICY cm_case_isolation ON hub_case
    FOR ALL
    USING (
        program_id IN (
            SELECT program_id FROM user_program_assignment
            WHERE user_id = current_setting('app.current_user_id', true)::uuid
            AND (expires_at IS NULL OR expires_at > NOW())
        )
        OR EXISTS (
            SELECT 1 FROM hub_user
            WHERE id = current_setting('app.current_user_id', true)::uuid
            AND is_hub_admin = true
        )
    );

-- Cascade RLS to all case-child tables via case_id FK
-- Insurance Plan
CREATE POLICY cm_insurance_plan_isolation ON insurance_plan
    FOR ALL
    USING (
        case_id IN (SELECT id FROM hub_case)
    );

-- Benefits Verification
CREATE POLICY cm_benefits_verification_isolation ON benefits_verification
    FOR ALL
    USING (
        case_id IN (SELECT id FROM hub_case)
    );

-- Enrollment Record
CREATE POLICY cm_enrollment_record_isolation ON enrollment_record
    FOR ALL
    USING (
        case_id IN (SELECT id FROM hub_case)
    );

-- Workflow State
CREATE POLICY cm_workflow_state_isolation ON workflow_state
    FOR ALL
    USING (
        case_id IN (SELECT id FROM hub_case)
    );

-- Financial Assistance Case
CREATE POLICY cm_financial_assistance_isolation ON financial_assistance_case
    FOR ALL
    USING (
        case_id IN (SELECT id FROM hub_case)
    );

-- Prior Authorization
CREATE POLICY cm_prior_authorization_isolation ON prior_authorization
    FOR ALL
    USING (
        case_id IN (SELECT id FROM hub_case)
    );

-- Patient Outreach
CREATE POLICY cm_patient_outreach_isolation ON patient_outreach
    FOR ALL
    USING (
        case_id IN (SELECT id FROM hub_case)
    );

-- Case Task
CREATE POLICY cm_case_task_isolation ON case_task
    FOR ALL
    USING (
        case_id IN (SELECT id FROM hub_case)
    );

-- Interaction
CREATE POLICY cm_interaction_isolation ON interaction
    FOR ALL
    USING (
        case_id IN (SELECT id FROM hub_case)
    );

-- Case Status History
CREATE POLICY cm_case_status_history_isolation ON case_status_history
    FOR ALL
    USING (
        case_id IN (SELECT id FROM hub_case)
    );

-- Service Call Log (case_id can be null for non-case calls)
CREATE POLICY cm_service_call_log_isolation ON service_call_log
    FOR ALL
    USING (
        case_id IS NULL
        OR case_id IN (SELECT id FROM hub_case)
    );

-- AKS enforcement: Government-insured patients cannot receive copay assistance
CREATE POLICY aks_copay_prohibition ON financial_assistance_case
    FOR INSERT
    WITH CHECK (
        NOT (
            fa_type = 'COPAY'
            AND EXISTS (
                SELECT 1 FROM insurance_plan ip
                JOIN hub_case hc ON ip.case_id = hc.id
                WHERE hc.id = financial_assistance_case.case_id
                AND ip.insurance_type IN (
                    'MEDICARE', 'MEDICAID', 'TRICARE', 'VA', 'MEDICARE_ADVANTAGE'
                )
                AND ip.is_primary = true
            )
        )
    );
