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
    public Optional<Recipe> findByIdAndHouseholdId(UUID recipeId, UUID householdId) {
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        Recipe recipe = byId.get(recipeId);
        if (recipe == null || !householdId.equals(recipe.getHouseholdId())) {
            return Optional.empty();
        }
        return Optional.of(recipe);
    }

    @Override
    public List<Recipe> findByHouseholdId(UUID householdId) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : byId.values()) {
            if (householdId.equals(recipe.getHouseholdId())) {
                result.add(recipe);
            }
        }
        return result;
    }

    @Override
    public List<Recipe> findByHouseholdIdAndIds(UUID householdId, Set<UUID> recipeIds) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (recipeIds == null) {
            throw new IllegalArgumentException("recipeIds must not be null");
        }
        List<Recipe> result = new ArrayList<>();
        for (UUID recipeId : recipeIds) {
            Recipe recipe = byId.get(recipeId);
            if (recipe != null && householdId.equals(recipe.getHouseholdId())) {
                result.add(recipe);
            }
        }
        return result;
    }
}
