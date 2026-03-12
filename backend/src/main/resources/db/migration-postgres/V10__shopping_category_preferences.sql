CREATE TABLE shopping_category_preferences (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    normalized_title VARCHAR(255) NOT NULL,
    preferred_category VARCHAR(64) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_shopping_category_preferences_group
        FOREIGN KEY (group_id)
        REFERENCES groups(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_shopping_category_preferences_group_title UNIQUE (group_id, normalized_title)
);

CREATE INDEX idx_shopping_category_preferences_group_id
    ON shopping_category_preferences(group_id);
