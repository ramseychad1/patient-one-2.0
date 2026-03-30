-- V010: Performance indexes

-- HubCase lookups
CREATE INDEX idx_hub_case_program_id ON hub_case(program_id);
CREATE INDEX idx_hub_case_patient_id ON hub_case(patient_id);
CREATE INDEX idx_hub_case_assigned_cm ON hub_case(assigned_cm_id);
CREATE INDEX idx_hub_case_case_number ON hub_case(case_number);
CREATE INDEX idx_hub_case_workflow_state ON hub_case(current_workflow_state);
CREATE INDEX idx_hub_case_stage ON hub_case(current_stage);
CREATE INDEX idx_hub_case_sla_breach ON hub_case(sla_breach_flag) WHERE sla_breach_flag = true;

-- WorkflowState: find current state per case
CREATE INDEX idx_workflow_state_case_current ON workflow_state(case_id, is_current) WHERE is_current = true;

-- CaseTask: CM's open task queue
CREATE INDEX idx_case_task_assigned_open ON case_task(assigned_to, status) WHERE status IN ('OPEN', 'IN_PROGRESS');
CREATE INDEX idx_case_task_case_id ON case_task(case_id);
CREATE INDEX idx_case_task_due_at ON case_task(due_at) WHERE status = 'OPEN';
CREATE INDEX idx_case_task_sla_breach ON case_task(sla_breached) WHERE sla_breached = true;

-- Interaction: timeline per case
CREATE INDEX idx_interaction_case_id ON interaction(case_id);
CREATE INDEX idx_interaction_created_at ON interaction(case_id, created_at DESC);

-- CaseStatusHistory: audit trail per case
CREATE INDEX idx_case_status_history_case ON case_status_history(case_id);
CREATE INDEX idx_case_status_history_changed_at ON case_status_history(case_id, changed_at DESC);

-- ServiceCallLog: per case
CREATE INDEX idx_service_call_log_case ON service_call_log(case_id);

-- InsurancePlan: per case
CREATE INDEX idx_insurance_plan_case ON insurance_plan(case_id);

-- BenefitsVerification: per case
CREATE INDEX idx_benefits_verification_case ON benefits_verification(case_id);

-- EnrollmentRecord: per case
CREATE INDEX idx_enrollment_record_case ON enrollment_record(case_id);

-- FinancialAssistanceCase: per case
CREATE INDEX idx_financial_assistance_case_case ON financial_assistance_case(case_id);

-- PriorAuthorization: per case
CREATE INDEX idx_prior_authorization_case ON prior_authorization(case_id);

-- PatientOutreach: per case
CREATE INDEX idx_patient_outreach_case ON patient_outreach(case_id);

-- UserProgramAssignment: program access check
CREATE INDEX idx_user_program_assignment_user ON user_program_assignment(user_id);
CREATE INDEX idx_user_program_assignment_program ON user_program_assignment(program_id);

-- UserRole: role lookup
CREATE INDEX idx_user_role_user ON user_role(user_id);

-- Patient: lookup by name+dob for dedup
CREATE INDEX idx_patient_name_dob ON patient(last_name, first_name, date_of_birth);
