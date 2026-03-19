alter table recipes
    add column saved_in_recipes boolean;

update recipes
set saved_in_recipes = true
where saved_in_recipes is null;

alter table recipes
    alter column saved_in_recipes set not null;

alter table recipes
    alter column saved_in_recipes set default true;
