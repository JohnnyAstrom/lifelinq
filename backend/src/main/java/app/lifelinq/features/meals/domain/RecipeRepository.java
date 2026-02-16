package app.lifelinq.features.meals.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RecipeRepository {
    Recipe save(Recipe recipe);

    Optional<Recipe> findByIdAndHouseholdId(UUID recipeId, UUID householdId);

    List<Recipe> findByHouseholdId(UUID householdId);

    List<Recipe> findByHouseholdIdAndIds(UUID householdId, Set<UUID> recipeIds);
}
