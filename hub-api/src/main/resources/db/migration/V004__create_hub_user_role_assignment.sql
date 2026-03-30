-- V004: HubUser, Role, UserRole, UserProgramAssignment (Layer 7 - Security)
-- Created before HubCase because hub_case references hub_user

CREATE TABLE hub_user (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(200) NOT NULL UNIQUE,
    password_hash   VARCHAR(300) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    display_name    VARCHAR(200),
    phone           VARCHAR(20),
    is_hub_admin    BOOLEAN NOT NULL DEFAULT false,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    last_login_at   TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      UUID REFERENCES hub_user(id)
);

CREATE TABLE role (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(300)
);

CREATE TABLE user_role (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES hub_user(id),
    role_id     UUID NOT NULL REFERENCES role(id),
    assigned_at TIMESTAMP NOT NULL DEFAULT NOW(),
    assigned_by UUID REFERENCES hub_user(id),
    UNIQUE (user_id, role_id)
);

CREATE TABLE user_program_assignment (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES hub_user(id),
    program_id          UUID NOT NULL REFERENCES program(id),
    can_create_cases    BOOLEAN NOT NULL DEFAULT true,
    can_edit_cases      BOOLEAN NOT NULL DEFAULT true,
    can_close_cases     BOOLEAN NOT NULL DEFAULT false,
    can_view_financials BOOLEAN NOT NULL DEFAULT true,
    can_perform_actions BOOLEAN NOT NULL DEFAULT true,
    expires_at          TIMESTAMP,
    assigned_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    assigned_by         UUID REFERENCES hub_user(id),
    UNIQUE (user_id, program_id)
);

-- Add FK from manufacturer.created_by now that hub_user exists
ALTER TABLE manufacturer ADD CONSTRAINT fk_manufacturer_created_by
    FOREIGN KEY (created_by) REFERENCES hub_user(id);
ALTER TABLE program ADD CONSTRAINT fk_program_created_by
    FOREIGN KEY (created_by) REFERENCES hub_user(id);
ALTER TABLE program_config ADD CONSTRAINT fk_program_config_updated_by
    FOREIGN KEY (updated_by) REFERENCES hub_user(id);
