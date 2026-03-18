alter table planned_meals
    add column meal_title varchar(255);

update planned_meals
set meal_title = recipe_title_snapshot
where meal_title is null;

alter table planned_meals
    alter column meal_title set not null;

alter table planned_meals
    alter column recipe_id drop not null;

alter table planned_meals
    alter column recipe_title_snapshot drop not null;
