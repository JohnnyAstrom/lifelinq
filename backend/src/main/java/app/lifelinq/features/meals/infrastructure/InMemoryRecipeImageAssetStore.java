package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.application.RecipeImportFailedException;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeKind;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import app.lifelinq.features.meals.contract.RecipeImageAssetPayload;
import app.lifelinq.features.meals.contract.RecipeImageAssetStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryRecipeImageAssetStore implements RecipeImageAssetStore {
    private final Map<String, RecipeImageAssetPayload> byReferenceId = new ConcurrentHashMap<>();

    @Override
    public RecipeAssetIntakeReference stageImage(
            String sourceLabel,
            String originalFilename,
            String mimeType,
            byte[] content
    ) {
        String referenceId = UUID.randomUUID().toString();
        RecipeImageAssetPayload payload = new RecipeImageAssetPayload(
                referenceId,
                sourceLabel,
                originalFilename,
                mimeType,
                content
        );
        byReferenceId.put(referenceId, payload);
        return new RecipeAssetIntakeReference(
                RecipeAssetIntakeKind.IMAGE,
                referenceId,
                payload.sourceLabel(),
                payload.originalFilename(),
                payload.mimeType()
        );
    }

    @Override
    public RecipeImageAssetPayload loadImage(String referenceId) {
        RecipeImageAssetPayload payload = byReferenceId.get(referenceId);
        if (payload == null) {
            throw new RecipeImportFailedException("We could not open that photo. Try importing it again.");
        }
        return payload;
    }
}
