package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.Recipe;
import app.lifelinq.features.meals.domain.RecipeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class JpaRecipeRepositoryAdapter implements RecipeRepository {
    private final RecipeJpaRepository repository;
    private final RecipeIngredientJpaRepository ingredientRepository;
    private final RecipeMapper mapper;

    public JpaRecipeRepositoryAdapter(
            RecipeJpaRepository repository,
            RecipeIngredientJpaRepository ingredientRepository,
            RecipeMapper mapper
    ) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        if (ingredientRepository == null) {
            throw new IllegalArgumentException("ingredientRepository must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.repository = repository;
        this.ingredientRepository = ingredientRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Recipe save(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("recipe must not be null");
        }
        RecipeEntity saved = repository.findByIdAndGroupId(recipe.getId(), recipe.getGroupId())
                .map(existing -> updateManagedEntity(existing, recipe))
                .orElseGet(() -> repository.save(mapper.toEntity(recipe)));
        return mapper.toDomain(saved);
    }

    private RecipeEntity updateManagedEntity(RecipeEntity existing, Recipe recipe) {
        existing.updateContent(
                recipe.getName(),
                recipe.getSourceName(),
                recipe.getSourceUrl(),
                recipe.getOriginKind(),
                recipe.getShortNote(),
                recipe.getInstructions(),
                recipe.getUpdatedAt()
        );
        existing.getIngredients().clear();
        ingredientRepository.deleteByRecipeId(recipe.getId());
        repository.flush();
        existing.replaceIngredients(mapper.toIngredientEntities(recipe, existing));
        return repository.saveAndFlush(existing);
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
