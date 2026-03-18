alter table recipe_ingredients
    add column raw_text varchar(1000);

update recipe_ingredients
set raw_text = name
where raw_text is null;
