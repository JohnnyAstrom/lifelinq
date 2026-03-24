CREATE TABLE recipe_drafts (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    name VARCHAR(255),
    source_name VARCHAR(255),
    source_url VARCHAR(1000),
    origin_kind VARCHAR(32) NOT NULL,
    servings VARCHAR(255),
    short_note VARCHAR(1000),
    instructions TEXT,
    draft_state VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_recipe_drafts_group_id ON recipe_drafts(group_id);

CREATE TABLE recipe_draft_ingredients (
    id UUID PRIMARY KEY,
    recipe_draft_id UUID NOT NULL REFERENCES recipe_drafts(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    raw_text VARCHAR(2000),
    quantity NUMERIC(18, 4),
    unit VARCHAR(16),
    position INTEGER NOT NULL
);

CREATE INDEX idx_recipe_draft_ingredients_draft_id ON recipe_draft_ingredients(recipe_draft_id);
