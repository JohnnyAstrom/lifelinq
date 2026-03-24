package app.lifelinq.features.meals.application;

import app.lifelinq.features.group.contract.AccessDeniedException;
import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import app.lifelinq.features.meals.contract.RecipeImportDraftView;
import app.lifelinq.features.meals.contract.RecipeImportPort;
import java.util.UUID;

public class RecipeImportApplicationService {
    private final EnsureGroupMemberUseCase ensureGroupMemberUseCase;
    private final RecipeImportPort recipeImportPort;

    public RecipeImportApplicationService(
            EnsureGroupMemberUseCase ensureGroupMemberUseCase,
            RecipeImportPort recipeImportPort
    ) {
        if (ensureGroupMemberUseCase == null) {
            throw new IllegalArgumentException("ensureGroupMemberUseCase must not be null");
        }
        if (recipeImportPort == null) {
            throw new IllegalArgumentException("recipeImportPort must not be null");
        }
        this.ensureGroupMemberUseCase = ensureGroupMemberUseCase;
        this.recipeImportPort = recipeImportPort;
    }

    public RecipeImportDraftView importRecipeDraft(
            UUID groupId,
            UUID actorUserId,
            String sourceUrl
    ) {
        ensureMealAccess(groupId, actorUserId);
        return RecipeImportDraftSupport.toLegacyView(
                RecipeImportDraftSupport.importFromUrl(recipeImportPort, sourceUrl)
        );
    }

    private void ensureMealAccess(UUID groupId, UUID actorUserId) {
        try {
            ensureGroupMemberUseCase.execute(groupId, actorUserId);
        } catch (AccessDeniedException ex) {
            throw new MealsAccessDeniedException(ex.getMessage());
        }
    }
}
