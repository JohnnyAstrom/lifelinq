CREATE TABLE meal_preference_signals (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    target_kind VARCHAR(32) NOT NULL,
    recipe_id UUID,
    meal_identity_key VARCHAR(255),
    signal_type VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_meal_preference_signals_group_id ON meal_preference_signals(group_id);
