ALTER TABLE invitations
    ADD COLUMN short_code VARCHAR(6);

ALTER TABLE invitations
    ADD CONSTRAINT uk_invitations_short_code UNIQUE (short_code);
