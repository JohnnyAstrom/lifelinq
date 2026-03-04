CREATE TABLE groups (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    active_group_id UUID NULL,
    email VARCHAR(320) NULL,
    first_name VARCHAR(255) NULL,
    last_name VARCHAR(255) NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_active_group
        FOREIGN KEY (active_group_id)
        REFERENCES groups(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_users_active_group_id
    ON users(active_group_id);

CREATE TABLE memberships (
    group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(32) NOT NULL,
    CONSTRAINT pk_memberships PRIMARY KEY (group_id, user_id),
    CONSTRAINT fk_memberships_group
        FOREIGN KEY (group_id)
        REFERENCES groups(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_memberships_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_memberships_user_id
    ON memberships(user_id);

CREATE TABLE invitations (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    type VARCHAR(32) NOT NULL,
    invitee_email VARCHAR(320) NULL,
    inviter_display_name VARCHAR(255) NULL,
    token VARCHAR(255) NOT NULL,
    short_code VARCHAR(6) NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    max_uses INTEGER NULL,
    usage_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    CONSTRAINT fk_invitations_group
        FOREIGN KEY (group_id)
        REFERENCES groups(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_invitations_token UNIQUE (token),
    CONSTRAINT ck_invitations_type_invitee
        CHECK (
            (type = 'EMAIL' AND invitee_email IS NOT NULL AND btrim(invitee_email) <> '')
            OR
            (type = 'LINK' AND invitee_email IS NULL)
        ),
    CONSTRAINT ck_invitations_max_uses_positive
        CHECK (max_uses IS NULL OR max_uses > 0),
    CONSTRAINT ck_invitations_usage_count_non_negative
        CHECK (usage_count >= 0),
    CONSTRAINT ck_invitations_usage_with_max
        CHECK (max_uses IS NULL OR usage_count <= max_uses)
);

CREATE UNIQUE INDEX uk_invitations_short_code_not_null
    ON invitations(short_code)
    WHERE short_code IS NOT NULL;

CREATE INDEX idx_invitation_group_email_status
    ON invitations(group_id, invitee_email, status);

CREATE INDEX idx_invitations_status
    ON invitations(status);

CREATE INDEX idx_invitations_active_link_by_group
    ON invitations(group_id, expires_at)
    WHERE status = 'ACTIVE' AND type = 'LINK';
