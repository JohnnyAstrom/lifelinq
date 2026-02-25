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
    public Optional<Recipe> findByIdAndGroupId(UUID recipeId, UUID groupId) {
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        return repository.findByIdAndGroupId(recipeId, groupId).map(mapper::toDomain);
    }

    @Override
    public List<Recipe> findByGroupId(UUID groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        List<Recipe> result = new ArrayList<>();
        for (RecipeEntity entity : repository.findByGroupId(groupId)) {
            result.add(mapper.toDomain(entity));
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
        if (recipeIds.isEmpty()) {
            return List.of();
        }
        List<Recipe> result = new ArrayList<>();
        for (RecipeEntity entity : repository.findByGroupIdAndIdIn(groupId, recipeIds)) {
            result.add(mapper.toDomain(entity));
        }
        return result;
    }
}
