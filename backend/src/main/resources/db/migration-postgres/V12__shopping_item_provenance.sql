ALTER TABLE shopping_items
    ADD COLUMN source_kind VARCHAR(64) NULL,
    ADD COLUMN source_label VARCHAR(255) NULL;
