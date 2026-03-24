package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.RecipeDraft;
import app.lifelinq.features.meals.domain.RecipeDraftRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class JpaRecipeDraftRepositoryAdapter implements RecipeDraftRepository {
    private final RecipeDraftJpaRepository repository;
    private final RecipeDraftIngredientJpaRepository ingredientRepository;
    private final RecipeDraftMapper mapper;

    public JpaRecipeDraftRepositoryAdapter(
            RecipeDraftJpaRepository repository,
            RecipeDraftIngredientJpaRepository ingredientRepository,
            RecipeDraftMapper mapper
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
    public RecipeDraft save(RecipeDraft recipeDraft) {
        if (recipeDraft == null) {
            throw new IllegalArgumentException("recipeDraft must not be null");
        }
        RecipeDraftEntity saved = repository.findByIdAndGroupId(recipeDraft.getId(), recipeDraft.getGroupId())
                .map(existing -> updateManagedEntity(existing, recipeDraft))
                .orElseGet(() -> repository.save(mapper.toEntity(recipeDraft)));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<RecipeDraft> findByIdAndGroupId(UUID draftId, UUID groupId) {
        if (draftId == null) {
            throw new IllegalArgumentException("draftId must not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        return repository.findByIdAndGroupId(draftId, groupId).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void delete(RecipeDraft recipeDraft) {
        if (recipeDraft == null) {
            throw new IllegalArgumentException("recipeDraft must not be null");
        }
        ingredientRepository.deleteByRecipeDraftId(recipeDraft.getId());
        repository.deleteByIdAndGroupId(recipeDraft.getId(), recipeDraft.getGroupId());
    }

    private RecipeDraftEntity updateManagedEntity(RecipeDraftEntity existing, RecipeDraft recipeDraft) {
        existing.updateContent(
                recipeDraft.getName(),
                recipeDraft.getSource().sourceName(),
                recipeDraft.getSource().sourceUrl(),
                recipeDraft.getProvenance().originKind(),
                recipeDraft.getServings(),
                recipeDraft.getShortNote(),
                recipeDraft.getInstructions().body(),
                recipeDraft.getState(),
                recipeDraft.getUpdatedAt()
        );
        existing.getIngredients().clear();
        ingredientRepository.deleteByRecipeDraftId(recipeDraft.getId());
        repository.flush();
        existing.replaceIngredients(mapper.toIngredientEntities(recipeDraft, existing));
        return repository.saveAndFlush(existing);
    }
}
