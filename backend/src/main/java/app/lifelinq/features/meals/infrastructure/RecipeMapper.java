package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.Ingredient;
import app.lifelinq.features.meals.domain.Recipe;

final class RecipeMapper {

    RecipeEntity toEntity(Recipe recipe) {
        RecipeEntity entity = new RecipeEntity(
                recipe.getId(),
                recipe.getGroupId(),
                recipe.getName(),
                recipe.getCreatedAt()
        );
        for (Ingredient ingredient : recipe.getIngredients()) {
            entity.getIngredients().add(new RecipeIngredientEntity(
                    ingredient.getId(),
                    entity,
                    ingredient.getName(),
                    ingredient.getQuantity(),
                    ingredient.getUnit(),
                    ingredient.getPosition()
            ));
        }
        return entity;
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
                entity.getCreatedAt(),
                ingredients
        );
    }
}
