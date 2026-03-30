-- V001: Manufacturer table (Layer 1 - Tenant)
CREATE TABLE manufacturer (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(200) NOT NULL UNIQUE,
    primary_contact_name  VARCHAR(200),
    primary_contact_email VARCHAR(200),
    primary_contact_phone VARCHAR(20),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'INACTIVE')),
    contract_reference VARCHAR(200),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      UUID
);
