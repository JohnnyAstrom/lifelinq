package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.Recipe;
import app.lifelinq.features.meals.domain.RecipeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryRecipeRepository implements RecipeRepository {
    private final ConcurrentMap<UUID, Recipe> byId = new ConcurrentHashMap<>();

    @Override
    public Recipe save(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("recipe must not be null");
        }
        byId.put(recipe.getId(), recipe);
        return recipe;
    }

    @Override
    public Optional<Recipe> findByIdAndGroupId(UUID recipeId, UUID groupId) {
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        Recipe recipe = byId.get(recipeId);
        if (recipe == null || !groupId.equals(recipe.getGroupId())) {
            return Optional.empty();
        }
        return Optional.of(recipe);
    }

    @Override
    public List<Recipe> findByGroupId(UUID groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : byId.values()) {
            if (groupId.equals(recipe.getGroupId())) {
                result.add(recipe);
            }
        }
        return result;
    }

    @Override
    public List<Recipe> findByGroupIdAndIds(UUID groupId, Set<UUID> recipeIds) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (recipeIds == null) {
            throw new IllegalArgumentException("recipeIds must not be null");
        }
        List<Recipe> result = new ArrayList<>();
        for (UUID recipeId : recipeIds) {
            Recipe recipe = byId.get(recipeId);
            if (recipe != null && groupId.equals(recipe.getGroupId())) {
                result.add(recipe);
            }
        }
        return result;
    }
}
