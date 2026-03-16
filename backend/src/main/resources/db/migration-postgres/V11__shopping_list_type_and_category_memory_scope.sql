ALTER TABLE shopping_lists
    ADD COLUMN list_type VARCHAR(64) NOT NULL DEFAULT 'mixed';

ALTER TABLE shopping_category_preferences
    ADD COLUMN list_type VARCHAR(64) NOT NULL DEFAULT 'mixed';

ALTER TABLE shopping_category_preferences
    DROP CONSTRAINT uk_shopping_category_preferences_group_title;

ALTER TABLE shopping_category_preferences
    ADD CONSTRAINT uk_shopping_category_preferences_group_title
        UNIQUE (group_id, list_type, normalized_title);
