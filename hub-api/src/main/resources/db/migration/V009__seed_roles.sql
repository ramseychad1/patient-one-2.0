-- V009: Seed role data

INSERT INTO role (id, name, description) VALUES
    (gen_random_uuid(), 'CASE_MANAGER', 'End-to-end patient journey management within assigned programs'),
    (gen_random_uuid(), 'SUPERVISOR', 'Team oversight, SLA monitoring, escalations'),
    (gen_random_uuid(), 'REIMBURSEMENT_COORDINATOR', 'BI, PA, appeals, payer follow-up'),
    (gen_random_uuid(), 'CARE_COORDINATOR', 'Intake, inbound/outbound calls, enrollment'),
    (gen_random_uuid(), 'FIELD_SOLUTIONS', 'HCP support, field rep requests'),
    (gen_random_uuid(), 'MANUFACTURER_VIEWER', 'Read-only access to program data'),
    (gen_random_uuid(), 'HUB_ADMIN', 'System configuration, all programs, all manufacturers');
