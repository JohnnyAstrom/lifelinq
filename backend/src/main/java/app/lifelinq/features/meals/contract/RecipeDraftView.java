package app.lifelinq.features.meals.contract;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecipeDraftView(
        UUID draftId,
        UUID groupId,
        String state,
        String name,
        RecipeSourceView source,
        RecipeProvenanceView provenance,
        String servings,
        String shortNote,
        String instructions,
        Instant createdAt,
        Instant updatedAt,
        List<IngredientView> ingredients
) {
}
