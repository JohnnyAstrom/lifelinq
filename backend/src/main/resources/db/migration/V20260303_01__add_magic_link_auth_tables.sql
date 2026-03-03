CREATE TABLE auth_magic_link_challenges (
    id UUID PRIMARY KEY,
    token VARCHAR(128) NOT NULL UNIQUE,
    email VARCHAR(320) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE NULL
);

CREATE INDEX idx_auth_magic_link_challenges_email ON auth_magic_link_challenges(email);
CREATE INDEX idx_auth_magic_link_challenges_expires_at ON auth_magic_link_challenges(expires_at);

CREATE TABLE auth_identities (
    id UUID PRIMARY KEY,
    provider VARCHAR(32) NOT NULL,
    subject VARCHAR(320) NOT NULL,
    user_id UUID NOT NULL
);

CREATE UNIQUE INDEX uk_auth_identities_provider_subject ON auth_identities(provider, subject);
CREATE INDEX idx_auth_identities_user_id ON auth_identities(user_id);

