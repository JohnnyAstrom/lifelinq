package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.Ingredient;
import app.lifelinq.features.meals.domain.RecipeDraft;
import app.lifelinq.features.meals.domain.RecipeInstructions;
import app.lifelinq.features.meals.domain.RecipeProvenance;
import app.lifelinq.features.meals.domain.RecipeSource;

final class RecipeDraftMapper {

    RecipeDraftEntity toEntity(RecipeDraft recipeDraft) {
        RecipeDraftEntity entity = new RecipeDraftEntity(
                recipeDraft.getId(),
                recipeDraft.getGroupId(),
                recipeDraft.getName(),
                recipeDraft.getSource().sourceName(),
                recipeDraft.getSource().sourceUrl(),
                recipeDraft.getProvenance().originKind(),
                recipeDraft.getServings(),
                recipeDraft.getShortNote(),
                recipeDraft.getInstructions().body(),
                recipeDraft.getState(),
                recipeDraft.getCreatedAt(),
                recipeDraft.getUpdatedAt()
        );
        entity.replaceIngredients(toIngredientEntities(recipeDraft, entity));
        return entity;
    }

    java.util.List<RecipeDraftIngredientEntity> toIngredientEntities(RecipeDraft recipeDraft, RecipeDraftEntity entity) {
        java.util.List<RecipeDraftIngredientEntity> ingredients = new java.util.ArrayList<>();
        for (Ingredient ingredient : recipeDraft.getIngredients()) {
            ingredients.add(new RecipeDraftIngredientEntity(
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

    RecipeDraft toDomain(RecipeDraftEntity entity) {
        java.util.List<Ingredient> ingredients = new java.util.ArrayList<>();
        for (RecipeDraftIngredientEntity ingredient : entity.getIngredients()) {
            ingredients.add(new Ingredient(
                    ingredient.getId(),
                    ingredient.getName(),
                    ingredient.getRawText(),
                    ingredient.getQuantity(),
                    ingredient.getUnit(),
                    ingredient.getPosition()
            ));
        }
        return new RecipeDraft(
                entity.getId(),
                entity.getGroupId(),
                entity.getName(),
                new RecipeSource(entity.getSourceName(), entity.getSourceUrl()),
                new RecipeProvenance(entity.getOriginKind(), entity.getSourceUrl()),
                entity.getServings(),
                entity.getShortNote(),
                new RecipeInstructions(entity.getInstructions()),
                entity.getDraftState(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                ingredients
        );
    }
}
