CREATE TABLE auth_magic_link_challenges (
    id UUID PRIMARY KEY,
    token VARCHAR(128) NOT NULL UNIQUE,
    email VARCHAR(320) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_auth_magic_link_challenges_email
    ON auth_magic_link_challenges(email);

CREATE INDEX idx_auth_magic_link_challenges_expires_at
    ON auth_magic_link_challenges(expires_at);

CREATE TABLE auth_identities (
    id UUID PRIMARY KEY,
    provider VARCHAR(32) NOT NULL,
    subject VARCHAR(320) NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT fk_auth_identities_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_auth_identities_provider_subject UNIQUE (provider, subject),
    CONSTRAINT ck_auth_identities_provider
        CHECK (provider IN ('EMAIL', 'GOOGLE', 'APPLE'))
);

CREATE INDEX idx_auth_identities_user_id
    ON auth_identities(user_id);
