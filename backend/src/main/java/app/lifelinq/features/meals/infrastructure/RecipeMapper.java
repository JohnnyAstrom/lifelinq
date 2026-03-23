package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.Ingredient;
import app.lifelinq.features.meals.domain.Recipe;

final class RecipeMapper {

    RecipeEntity toEntity(Recipe recipe) {
        RecipeEntity entity = new RecipeEntity(
                recipe.getId(),
                recipe.getGroupId(),
                recipe.getName(),
                recipe.getSourceName(),
                recipe.getSourceUrl(),
                recipe.getOriginKind(),
                recipe.getServings(),
                recipe.getMakeSoonAt(),
                recipe.getShortNote(),
                recipe.getInstructions(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt(),
                recipe.getArchivedAt(),
                recipe.isSavedInRecipes()
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
                    ingredient.getRawText(),
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
                    ingredient.getRawText(),
                    ingredient.getQuantity(),
                    ingredient.getUnit(),
                    ingredient.getPosition()
            ));
        }
        return new Recipe(
                entity.getId(),
                entity.getGroupId(),
                entity.getName(),
                entity.getSourceName(),
                entity.getSourceUrl(),
                entity.getOriginKind(),
                entity.getServings(),
                entity.getMakeSoonAt(),
                entity.getShortNote(),
                entity.getInstructions(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getArchivedAt(),
                entity.isSavedInRecipes(),
                ingredients
        );
    }
}
