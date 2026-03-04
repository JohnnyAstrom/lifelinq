CREATE TABLE auth_refresh_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    absolute_expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE NULL,
    revoke_reason VARCHAR(255) NULL,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_auth_refresh_sessions_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT ck_auth_refresh_sessions_revoke_fields
        CHECK (
            (revoked_at IS NULL AND revoke_reason IS NULL)
            OR
            (revoked_at IS NOT NULL AND revoke_reason IS NOT NULL)
        ),
    CONSTRAINT ck_auth_refresh_sessions_expiry_order
        CHECK (absolute_expires_at >= created_at)
);

CREATE TABLE auth_refresh_tokens (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    idle_expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE NULL,
    replaced_by_token_id UUID NULL,
    revoked_at TIMESTAMP WITH TIME ZONE NULL,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_auth_refresh_tokens_session
        FOREIGN KEY (session_id)
        REFERENCES auth_refresh_sessions(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_auth_refresh_tokens_replaced_by
        FOREIGN KEY (replaced_by_token_id)
        REFERENCES auth_refresh_tokens(id),
    CONSTRAINT ck_auth_refresh_tokens_replacement_usage_consistency
        CHECK (
            (replaced_by_token_id IS NULL AND used_at IS NULL)
            OR
            (replaced_by_token_id IS NOT NULL AND used_at IS NOT NULL)
        ),
    CONSTRAINT ck_auth_refresh_tokens_idle_expiry_order
        CHECK (idle_expires_at >= issued_at)
);

CREATE UNIQUE INDEX uk_auth_refresh_tokens_token_hash
    ON auth_refresh_tokens(token_hash);

CREATE INDEX idx_auth_refresh_tokens_session_id
    ON auth_refresh_tokens(session_id);

CREATE INDEX idx_auth_refresh_sessions_user_id
    ON auth_refresh_sessions(user_id);

CREATE INDEX idx_auth_refresh_tokens_idle_expires_at
    ON auth_refresh_tokens(idle_expires_at);

CREATE INDEX idx_auth_refresh_sessions_absolute_expires_at
    ON auth_refresh_sessions(absolute_expires_at);

CREATE INDEX idx_auth_refresh_tokens_replaced_by_token_id
    ON auth_refresh_tokens(replaced_by_token_id);
