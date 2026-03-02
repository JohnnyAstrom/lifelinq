ALTER TABLE invitations
    ADD COLUMN IF NOT EXISTS inviter_display_name VARCHAR(255);
