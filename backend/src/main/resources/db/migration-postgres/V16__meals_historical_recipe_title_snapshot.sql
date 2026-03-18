alter table planned_meals
    add column recipe_title_snapshot varchar(255);

update planned_meals pm
set recipe_title_snapshot = r.name
from week_plans wp
         join recipes r on r.id = pm.recipe_id and r.group_id = wp.group_id
where pm.week_plan_id = wp.id
  and pm.recipe_title_snapshot is null;

alter table planned_meals
    alter column recipe_title_snapshot set not null;
