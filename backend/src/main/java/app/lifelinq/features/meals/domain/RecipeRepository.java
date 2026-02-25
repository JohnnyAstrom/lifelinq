package app.lifelinq.features.meals.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RecipeRepository {
    Recipe save(Recipe recipe);

    Optional<Recipe> findByIdAndGroupId(UUID recipeId, UUID groupId);

    List<Recipe> findByGroupId(UUID groupId);

    List<Recipe> findByGroupIdAndIds(UUID groupId, Set<UUID> recipeIds);
}
