package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.Recipe;
import app.lifelinq.features.meals.domain.RecipeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class JpaRecipeRepositoryAdapter implements RecipeRepository {
    private final RecipeJpaRepository repository;
    private final RecipeMapper mapper;

    public JpaRecipeRepositoryAdapter(RecipeJpaRepository repository, RecipeMapper mapper) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Recipe save(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("recipe must not be null");
        }
        RecipeEntity saved = repository.save(mapper.toEntity(recipe));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Recipe> findByIdAndHouseholdId(UUID recipeId, UUID householdId) {
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        return repository.findByIdAndHouseholdId(recipeId, householdId).map(mapper::toDomain);
    }

    @Override
    public List<Recipe> findByHouseholdId(UUID householdId) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        List<Recipe> result = new ArrayList<>();
        for (RecipeEntity entity : repository.findByHouseholdId(householdId)) {
            result.add(mapper.toDomain(entity));
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
        if (recipeIds.isEmpty()) {
            return List.of();
        }
        List<Recipe> result = new ArrayList<>();
        for (RecipeEntity entity : repository.findByHouseholdIdAndIdIn(householdId, recipeIds)) {
            result.add(mapper.toDomain(entity));
        }
        return result;
    }
}
