ALTER TABLE week_plans
    ADD COLUMN shopping_review_handled_at TIMESTAMPTZ NULL,
    ADD COLUMN shopping_review_list_id UUID NULL;
