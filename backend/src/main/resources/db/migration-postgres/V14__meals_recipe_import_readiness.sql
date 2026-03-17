ALTER TABLE recipes
    ADD COLUMN source_name VARCHAR(255) NULL,
    ADD COLUMN source_url VARCHAR(1000) NULL,
    ADD COLUMN origin_kind VARCHAR(32) NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN updated_at TIMESTAMPTZ NULL;

UPDATE recipes
SET source_name = source,
    updated_at = created_at
WHERE source_name IS NULL
   OR updated_at IS NULL;

ALTER TABLE recipes
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE recipes
    DROP COLUMN source;
