CREATE TABLE shopping_lists (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_shopping_lists_group
        FOREIGN KEY (group_id)
        REFERENCES households(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_shopping_lists_group_id
    ON shopping_lists(group_id);

CREATE TABLE shopping_items (
    id UUID PRIMARY KEY,
    list_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    order_index INTEGER NULL,
    status VARCHAR(32) NOT NULL,
    quantity NUMERIC(12, 3) NULL,
    unit VARCHAR(32) NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    bought_at TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT fk_shopping_items_list
        FOREIGN KEY (list_id)
        REFERENCES shopping_lists(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_shopping_items_list_id
    ON shopping_items(list_id);

CREATE TABLE recipes (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_recipes_group
        FOREIGN KEY (group_id)
        REFERENCES households(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_recipes_group_id
    ON recipes(group_id);

CREATE TABLE recipe_ingredients (
    id UUID PRIMARY KEY,
    recipe_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity NUMERIC(12, 3) NULL,
    unit VARCHAR(32) NULL,
    position INTEGER NOT NULL,
    CONSTRAINT fk_recipe_ingredients_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipes(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_recipe_ingredients_recipe_position UNIQUE (recipe_id, position)
);

CREATE INDEX idx_recipe_ingredients_recipe_id
    ON recipe_ingredients(recipe_id);

CREATE TABLE week_plans (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    week_year INTEGER NOT NULL,
    iso_week INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_week_plans_group
        FOREIGN KEY (group_id)
        REFERENCES households(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_week_plans_group_week UNIQUE (group_id, week_year, iso_week)
);

CREATE INDEX idx_week_plans_group_id
    ON week_plans(group_id);

CREATE TABLE planned_meals (
    week_plan_id UUID NOT NULL,
    day_of_week INTEGER NOT NULL,
    meal_type VARCHAR(32) NOT NULL,
    recipe_id UUID NOT NULL,
    CONSTRAINT pk_planned_meals PRIMARY KEY (week_plan_id, day_of_week, meal_type),
    CONSTRAINT fk_planned_meals_week_plan
        FOREIGN KEY (week_plan_id)
        REFERENCES week_plans(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_planned_meals_week_plan_id
    ON planned_meals(week_plan_id);

CREATE INDEX idx_planned_meals_week_plan_day
    ON planned_meals(week_plan_id, day_of_week, meal_type);
