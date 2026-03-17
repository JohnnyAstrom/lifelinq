package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.Ingredient;
import app.lifelinq.features.meals.domain.Recipe;

final class RecipeMapper {

    RecipeEntity toEntity(Recipe recipe) {
        RecipeEntity entity = new RecipeEntity(
                recipe.getId(),
                recipe.getGroupId(),
                recipe.getName(),
                recipe.getSource(),
                recipe.getShortNote(),
                recipe.getInstructions(),
                recipe.getCreatedAt()
        );
        entity.replaceIngredients(toIngredientEntities(recipe, entity));
        return entity;
    }

    java.util.List<RecipeIngredientEntity> toIngredientEntities(Recipe recipe, RecipeEntity entity) {
        java.util.List<RecipeIngredientEntity> ingredients = new java.util.ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredients.add(new RecipeIngredientEntity(
                    ingredient.getId(),
                    entity,
                    ingredient.getName(),
                    ingredient.getQuantity(),
                    ingredient.getUnit(),
                    ingredient.getPosition()
            ));
        }
        return ingredients;
    }

    Recipe toDomain(RecipeEntity entity) {
        java.util.List<Ingredient> ingredients = new java.util.ArrayList<>();
        for (RecipeIngredientEntity ingredient : entity.getIngredients()) {
            ingredients.add(new Ingredient(
                    ingredient.getId(),
                    ingredient.getName(),
                    ingredient.getQuantity(),
                    ingredient.getUnit(),
                    ingredient.getPosition()
            ));
        }
        return new Recipe(
                entity.getId(),
                entity.getGroupId(),
                entity.getName(),
                entity.getSource(),
                entity.getShortNote(),
                entity.getInstructions(),
                entity.getCreatedAt(),
                ingredients
        );
    }
}
