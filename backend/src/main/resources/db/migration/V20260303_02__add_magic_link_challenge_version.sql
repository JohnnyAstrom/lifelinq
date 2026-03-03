ALTER TABLE auth_magic_link_challenges
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
