package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.RecipeDraft;
import app.lifelinq.features.meals.domain.RecipeDraftRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryRecipeDraftRepository implements RecipeDraftRepository {
    private final ConcurrentMap<UUID, RecipeDraft> byId = new ConcurrentHashMap<>();

    @Override
    public RecipeDraft save(RecipeDraft recipeDraft) {
        if (recipeDraft == null) {
            throw new IllegalArgumentException("recipeDraft must not be null");
        }
        byId.put(recipeDraft.getId(), recipeDraft);
        return recipeDraft;
    }

    @Override
    public Optional<RecipeDraft> findByIdAndGroupId(UUID draftId, UUID groupId) {
        if (draftId == null) {
            throw new IllegalArgumentException("draftId must not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        RecipeDraft recipeDraft = byId.get(draftId);
        if (recipeDraft == null || !groupId.equals(recipeDraft.getGroupId())) {
            return Optional.empty();
        }
        return Optional.of(recipeDraft);
    }

    @Override
    public void delete(RecipeDraft recipeDraft) {
        if (recipeDraft == null) {
            throw new IllegalArgumentException("recipeDraft must not be null");
        }
        byId.remove(recipeDraft.getId());
    }
}
