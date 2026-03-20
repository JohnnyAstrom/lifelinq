ALTER TABLE planned_meals
    ADD COLUMN shopping_handled_at TIMESTAMPTZ NULL,
    ADD COLUMN shopping_list_id UUID NULL;
