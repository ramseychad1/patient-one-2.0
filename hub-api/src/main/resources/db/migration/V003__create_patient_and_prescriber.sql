-- V003: Patient and Prescriber tables (Layer 2 - Case Core)

CREATE TABLE patient (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name              VARCHAR(100) NOT NULL,   -- PHI
    last_name               VARCHAR(100) NOT NULL,   -- PHI
    date_of_birth           DATE NOT NULL,           -- PHI
    gender                  VARCHAR(10)
                            CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'UNKNOWN')),
    address_line1           VARCHAR(200),            -- PHI
    address_line2           VARCHAR(200),            -- PHI
    city                    VARCHAR(100),            -- PHI
    state                   CHAR(2),                 -- PHI
    zip                     VARCHAR(10),             -- PHI
    phone_primary           VARCHAR(20),             -- PHI
    phone_secondary         VARCHAR(20),             -- PHI
    email                   VARCHAR(200),            -- PHI
    preferred_contact_method VARCHAR(10)
                            CHECK (preferred_contact_method IN ('SMS', 'PHONE', 'EMAIL')),
    preferred_language      VARCHAR(10),
    household_size          INTEGER,
    annual_income_usd       DECIMAL(12,2),           -- PHI
    income_verified_method  VARCHAR(20)
                            CHECK (income_verified_method IN ('EIV', 'PAYSTUB', 'TAX_RETURN', 'ATTESTATION')),
    income_verified_at      TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE prescriber (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    npi             VARCHAR(10) NOT NULL UNIQUE,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    practice_name   VARCHAR(200),
    specialty       VARCHAR(200),
    address_line1   VARCHAR(200),
    city            VARCHAR(100),
    state           CHAR(2),
    zip             VARCHAR(10),
    phone           VARCHAR(20),
    fax             VARCHAR(20),
    email           VARCHAR(200),
    dea_number      VARCHAR(20),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
