package app.lifelinq.features.meals.domain;

import java.util.Optional;
import java.util.UUID;

public interface RecipeDraftRepository {
    RecipeDraft save(RecipeDraft recipeDraft);

    Optional<RecipeDraft> findByIdAndGroupId(UUID draftId, UUID groupId);

    void delete(RecipeDraft recipeDraft);
}
