ALTER TABLE recipes
    ADD COLUMN archived_at TIMESTAMPTZ NULL;

CREATE INDEX idx_recipes_group_archived_at
    ON recipes (group_id, archived_at);
